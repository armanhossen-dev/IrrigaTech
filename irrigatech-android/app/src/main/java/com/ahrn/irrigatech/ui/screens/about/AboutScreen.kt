package com.ahrn.irrigatech.ui.screens.about

import android.content.Intent
import android.net.Uri

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.ahrn.irrigatech.BuildConfig
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.ui.components.InfoRow
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun openWhatsApp(phone: String) {
        openUrl("https://wa.me/$phone")
    }

    fun openAhrnWebsite() {
        openUrl("https://ahrn.vercel.app/")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {

        // ---------------------------------------------------------
        // Top Bar
        // ---------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.xs,
                    vertical = Spacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        // ---------------------------------------------------------
        // App Header
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_logo),
                    contentDescription = stringResource(R.string.cd_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp)),
                )
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ---------------------------------------------------------
        // Project Description
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        ) {
            Text(
                text = "About IrriGaTech",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = "IrriGaTech is a smart irrigation companion designed " +
                        "to make agricultural water management more efficient. " +
                        "The system monitors soil moisture, temperature and water " +
                        "tank level while controlling two irrigation pumps through " +
                        "the Blynk IoT cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = "The project combines agricultural knowledge, " +
                        "electronics, IoT technology and Android software to " +
                        "reduce unnecessary water usage while maintaining " +
                        "healthy crop conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.lg))

            InfoRow(label = "Platform", value = "Android")
            InfoRow(label = "Data source", value = "Blynk IoT Cloud REST")
            InfoRow(label = "Package", value = BuildConfig.APPLICATION_ID)
            InfoRow(label = "Technology", value = "Kotlin + Jetpack Compose")
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ---------------------------------------------------------
        // Project Team
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        ) {
            Text(
                text = "Project team",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "The people involved in designing, developing and " +
                        "integrating the IrriGaTech smart irrigation system.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.lg))

            // ---------------------------------------------------------
            // Team Member 1: EEE (Shoaib)
            // ---------------------------------------------------------
            TeamMemberRow(
                icon = Icons.Outlined.Engineering,
                name = "Md. Shoaib Bin Yousuf",
                department = "Electrical And Electronic Engineering",
                role = "IoT & Hardware Integration",
                description = "Contributes to the electrical and electronic aspects " +
                        "of the irrigation system, including sensors, pump control, " +
                        "hardware connectivity and IoT integration.",
                photoResId = R.drawable.shoaib,
                socialLinks = listOf(
                    SocialLink(
                        iconResId = R.drawable.ic_whatsapp,
                        contentDescription = "WhatsApp",
                        onClick = { openWhatsApp("8801798978244") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_facebook,
                        contentDescription = "Facebook",
                        onClick = { openUrl("https://www.facebook.com/md.shoaib.bin.yousuf") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_linkedin,
                        contentDescription = "LinkedIn",
                        onClick = { openUrl("https://linkedin.com/in/shoaib") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_web,
                        contentDescription = "Portfolio",
                        onClick = { openUrl("https://studentshub.daffodilvarsity.edu.bd/portfolio/mdshoaib-yousuf-253-33-394/") },
                    ),
                ),
            )

            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.lg))

            // ---------------------------------------------------------
            // Team Member 2: Agri (Monir)
            // ---------------------------------------------------------
            TeamMemberRow(
                icon = Icons.Outlined.Agriculture,
                name = "Md. Al Mozahid Monir",
                department = "Agricultural Science",
                role = "Agricultural & Crop Management",
                description = "Contributes agricultural expertise to the project, " +
                        "including crop requirements, irrigation needs, soil conditions " +
                        "and practical agricultural application of the system.",
                photoResId = R.drawable.monir,
                socialLinks = listOf(
                    SocialLink(
                        iconResId = R.drawable.ic_whatsapp,
                        contentDescription = "WhatsApp",
                        onClick = { openWhatsApp("8801408246383") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_facebook,
                        contentDescription = "Facebook",
                        onClick = { openUrl("https://www.facebook.com/almozahid.monir") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_linkedin,
                        contentDescription = "LinkedIn",
                        onClick = { openUrl("https://linkedin.com/in/almozahid") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_web,
                        contentDescription = "Portfolio",
                        onClick = { openUrl("#") },
                    ),
                ),
            )

            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.lg))

            // ---------------------------------------------------------
            // Team Member 3: CSE (Arman)
            // ---------------------------------------------------------
            TeamMemberRow(
                icon = Icons.Outlined.Code,
                name = "Md. Arman Hossen Ripon",
                department = "Computer Science & Engineering",
                role = "Lead Developer & System Integration",
                description = "Responsible for Android application development, " +
                        "software architecture, UI/UX implementation, Blynk IoT " +
                        "integration and overall system coordination.",
                photoResId = R.drawable.arman,
                primaryButtonText = "Visit site",
                primaryButtonIcon = Icons.Outlined.OpenInNew,
                onPrimaryButtonClick = ::openAhrnWebsite,
                socialLinks = listOf(
                    SocialLink(
                        iconResId = R.drawable.ic_github,
                        contentDescription = "GitHub",
                        onClick = { openUrl("https://github.com/armanhossen-dev/") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_linkedin,
                        contentDescription = "LinkedIn",
                        onClick = { openUrl("https://www.linkedin.com/in/armanhossenripon/") },
                    ),
                    SocialLink(
                        iconResId = R.drawable.ic_web,
                        contentDescription = "Portfolio",
                        onClick = { openUrl("https://www.armanhossen.is-a.dev/") },
                    ),
                ),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ---------------------------------------------------------
        // Development Credits
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Development & collaboration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = "IrriGaTech is a collaborative project combining " +
                        "agricultural science, electrical & electronic engineering, " +
                        "IoT and modern Android software development.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.lg))

            Button(
                onClick = ::openAhrnWebsite,
                shape = RoundedCornerShape(Radii.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(Modifier.size(Spacing.xs))

                Text(
                    text = "AHRN — Project Developer",
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = "Save Water · Save Life",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}


// =====================================================================
// Social link model + icon button
// =====================================================================

/**
 * Represents a single social/contact link rendered as a small circular
 * icon button. Provide either [icon] (a built-in Material vector) or
 * [iconResId] (a custom drawable, e.g. a brand mark such as WhatsApp,
 * Facebook or GitHub that isn't part of the default Material icon set).
 */
private data class SocialLink(
    val icon: ImageVector? = null,
    val iconResId: Int? = null,
    val contentDescription: String,
    val onClick: () -> Unit,
)

@Composable
private fun SocialIconButton(link: SocialLink) {
    val painter: Painter? = link.iconResId?.let { painterResource(it) }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = link.onClick, modifier = Modifier.size(36.dp)) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = link.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            } else if (link.icon != null) {
                Icon(
                    imageVector = link.icon,
                    contentDescription = link.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}


// =====================================================================
// Team Member Row
// =====================================================================

@Composable
private fun TeamMemberRow(
    icon: ImageVector,
    name: String,
    department: String,
    role: String,
    description: String,
    photoResId: Int? = null,
    primaryButtonText: String? = null,
    primaryButtonIcon: ImageVector? = null,
    onPrimaryButtonClick: (() -> Unit)? = null,
    socialLinks: List<SocialLink> = emptyList(),
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
            )

            Spacer(Modifier.size(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (photoResId != null) {
                Spacer(Modifier.width(Spacing.md))
                Image(
                    painter = painterResource(photoResId),
                    contentDescription = "Photo of $name",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        // Department shown as a professional-looking assist chip instead of
        // a plain label/value row.
        AssistChip(
            onClick = {},
            enabled = false,
            label = {
                Text(
                    text = department,
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = null,
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(Spacing.md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (primaryButtonText != null && primaryButtonIcon != null && onPrimaryButtonClick != null) {
                OutlinedButton(
                    onClick = onPrimaryButtonClick,
                    shape = RoundedCornerShape(Radii.button),
                ) {
                    Icon(
                        imageVector = primaryButtonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )

                    Spacer(Modifier.size(Spacing.xs))

                    Text(
                        text = primaryButtonText,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (socialLinks.isNotEmpty()) {
                    Spacer(Modifier.size(Spacing.sm))
                }
            }

            if (socialLinks.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    socialLinks.forEach { link -> SocialIconButton(link) }
                }
            }
        }
    }
}