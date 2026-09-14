package com.ahrn.irrigatech.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.data.local.SessionStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithGoogle(activity: Activity): Result<AuthUser>
    suspend fun signOut()
}

/**
 * Uses Firebase Google Sign-In when the project is configured (a
 * google-services.json is present at build time). Otherwise it falls back to a
 * local demo identity so the whole app stays explorable without Firebase.
 */
class DefaultAuthRepository(
    private val context: Context,
    private val session: SessionStore,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = session.user

    private val firebaseAvailable: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    override suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> {
        if (!firebaseAvailable) {
            return signInDemo()
        }
        return try {
            val user = firebaseSignIn(activity)
            session.save(user)
            Result.success(user)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.w(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        if (firebaseAvailable) {
            runCatching { FirebaseAuth.getInstance().signOut() }
        }
        session.clear()
    }

    private suspend fun firebaseSignIn(activity: Activity): AuthUser {
        val clientId = activity.getString(R.string.google_web_client_id)
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(activity, request)
        val credential = response.credential
        check(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type"
        }

        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val auth = FirebaseAuth.getInstance()
        val result = auth.signInWithCredential(firebaseCredential).await()
        val user = requireNotNull(result.user) { "Firebase returned no user" }
        return AuthUser(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl?.toString(),
        )
    }

    private suspend fun signInDemo(): Result<AuthUser> {
        val demo = AuthUser(
            uid = "demo-user",
            displayName = "Demo Farmer",
            email = "demo@irrigatech.app",
            photoUrl = null,
            isDemo = true,
        )
        session.save(demo)
        return Result.success(demo)
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
