package org.tech.ai.deepimage.metadata.handler;

import lombok.extern.slf4j.Slf4j;
import org.tech.ai.deepimage.metadata.FileMetaData;
import org.tech.ai.deepimage.metadata.ImageMetaData;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Image metadata handler implementation
 * Extracts metadata from image files using Java ImageIO API
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Slf4j
@Component
public class ImageMetaDataHandler implements FileMetaDataHandler {
    
    @Override
    public FileMetaData handle(byte[] fileBytes, String contentType) {
        if (fileBytes == null || fileBytes.length == 0) {
            log.warn("Cannot extract metadata from empty file bytes");
            return null;
        }
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes)) {
            BufferedImage image = ImageIO.read(bis);
            
            if (image == null) {
                log.warn("Failed to read image from bytes, contentType: {}", contentType);
                return null;
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            // Detect image format from content type
            String format = detectFormat(contentType);
            
            // Get color model information
            ColorModel colorModel = image.getColorModel();
            boolean hasAlpha = colorModel.hasAlpha();
            int bitDepth = colorModel.getPixelSize();
            
            // Get color space
            String colorSpace = colorModel.getColorSpace().getType() == ColorSpace.TYPE_RGB 
                    ? "RGB" 
                    : colorModel.getColorSpace().toString();
            
            return ImageMetaData.builder()
                    .width(width)
                    .height(height)
                    .format(format)
                    .colorSpace(colorSpace)
                    .hasAlpha(hasAlpha)
                    .bitDepth(bitDepth)
                    .orientation(null) // EXIF orientation extraction would require additional library
                    .build();
                    
        } catch (IOException e) {
            log.error("Error reading image for metadata extraction: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during image metadata extraction: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Detect image format from content type
     * 
     * @param contentType MIME type
     * @return Image format name (JPEG, PNG, GIF, etc.)
     */
    private String detectFormat(String contentType) {
        if (contentType == null) {
            return "UNKNOWN";
        }
        
        String lowerContentType = contentType.toLowerCase();
        if (lowerContentType.contains("jpeg") || lowerContentType.contains("jpg")) {
            return "JPEG";
        } else if (lowerContentType.contains("png")) {
            return "PNG";
        } else if (lowerContentType.contains("gif")) {
            return "GIF";
        } else if (lowerContentType.contains("bmp")) {
            return "BMP";
        } else if (lowerContentType.contains("webp")) {
            return "WEBP";
        } else {
            // Try to extract format from content type
            int slashIndex = contentType.indexOf('/');
            if (slashIndex > 0) {
                String subtype = contentType.substring(slashIndex + 1).toUpperCase();
                return subtype.contains("X-") ? subtype.substring(2) : subtype;
            }
            return "UNKNOWN";
        }
    }
}

