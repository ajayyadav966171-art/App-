package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        var isCodeBlock = false
        var codeContent = StringBuilder()

        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Code block logic
            if (trimmedLine.startsWith("```")) {
                if (isCodeBlock) {
                    // Close block and render
                    CodeBlockLayout(code = codeContent.toString().trimEnd())
                    codeContent = StringBuilder()
                    isCodeBlock = false
                } else {
                    isCodeBlock = true
                }
                continue
            }

            if (isCodeBlock) {
                codeContent.append(line).append("\n")
                continue
            }

            when {
                // Main Header
                trimmedLine.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = inlineMarkdownParser(trimmedLine.removePrefix("# ")),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 28.sp
                        )
                    )
                }
                // Sub Header
                trimmedLine.startsWith("## ") || trimmedLine.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = inlineMarkdownParser(trimmedLine.removePrefix("## ").removePrefix("# ")),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 22.sp
                        )
                    )
                }
                // Bullet List Items
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = "•  ",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = inlineMarkdownParser(trimmedLine.removePrefix("- ").removePrefix("* ")),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Numbered List Items
                trimmedLine.firstOrNull()?.isDigit() == true && trimmedLine.contains(". ") && trimmedLine.indexOf(". ") < 5 -> {
                    val dotIndex = trimmedLine.indexOf(". ")
                    val numPrefix = trimmedLine.substring(0, dotIndex + 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = numPrefix,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Text(
                            text = inlineMarkdownParser(trimmedLine.substring(dotIndex + 2)),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Normal paragraph (supports bold double asterisks block)
                trimmedLine.isNotEmpty() -> {
                    Text(
                        text = inlineMarkdownParser(line),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Spacers for blank lines
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun CodeBlockLayout(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Super lightweight parsing of bold text double-asterisks (e.g. **bold topic**)
 */
@Composable
private fun inlineMarkdownParser(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            val partText = parts[i]
            if (i % 2 == 1) { // Odd index is inside **bold**
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.inverseOnSurface)) {
                    append(partText)
                }
            } else {
                append(partText)
            }
        }
    }
}
