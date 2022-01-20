
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class TextureLoader {
    private final HashMap<String, Texture> table = new HashMap<String, Texture>();
    private final ColorModel glAlphaColorModel;
    private final ColorModel glColorModel;
    private final IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);

    public TextureLoader() {
        glAlphaColorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE
        );

        glColorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,0},
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE
        );
    }

    private int createTextureID() {
      glGenTextures(textureIDBuffer);
      return textureIDBuffer.get(0);
    }

    public Texture getTexture(String resourceName) throws IOException {
        Texture tex = table.get(resourceName);

        if (tex != null) {
            return tex;
        }

        tex = getTexture(resourceName, GL_TEXTURE_2D, GL_RGBA, GL_LINEAR, GL_LINEAR);
        table.put(resourceName,tex);

        return tex;
    }

    public Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) throws IOException {
        int srcPixelFormat;
        int textureID = createTextureID();
        Texture texture = new Texture(target,textureID);
        glBindTexture(target, textureID);

        BufferedImage bufferedImage = loadImage(resourceName);
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());

        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = GL_RGBA;
        } else {
            srcPixelFormat = GL_RGB;
        }

        ByteBuffer textureBuffer = convertImageData(bufferedImage,texture);

        if (target == GL_TEXTURE_2D) {
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter);
        }

        glTexImage2D(target, 0, dstPixelFormat, calculate2Times(bufferedImage.getWidth()),
                calculate2Times(bufferedImage.getHeight()), 0, srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);

        return texture;
    }

    private static int calculate2Times(int times) {
        int ret = 2;

        while (ret < times) {
            ret *= 2;
        }
        return ret;
    }


    private BufferedImage loadImage(String ref) throws IOException {
        URL url = TextureLoader.class.getClassLoader().getResource(ref);

        if (url == null) {
            throw new IOException("Introuvable: " + ref);
        }

        Image img = new ImageIcon(url).getImage();
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }

    private ByteBuffer convertImageData(BufferedImage bufferedImage,Texture texture) {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage image;

        int width = 2;
        int height = 2;

        while (width < bufferedImage.getWidth()) {
            width *= 2;
        }

        while (height < bufferedImage.getHeight()) {
            height *= 2;
        }

        texture.setTextureHeight(height);
        texture.setTextureWidth(width);

        if (bufferedImage.getColorModel().hasAlpha()) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height,4,null);
            image = new BufferedImage(glAlphaColorModel, raster,false, new Hashtable());
        } else {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height,3,null);
            image = new BufferedImage(glColorModel, raster,false, new Hashtable());
        }

        Graphics g = image.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0, width, height);
        g.drawImage(bufferedImage,0,0,null);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }
}
