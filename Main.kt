package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

private var alpha = false
var list: List<Int> = listOf(256, 1, 1)
var posMark: List<Int> = listOf(0, 0)
var wid = 0
var hei = 0

fun main() {
    println("Input the image filename:")
    val image = inputImage()
    println("Input the watermark image filename:")
    val watermark = inputWatermark(image)
    transparency(watermark)
    println("Input the watermark transparency percentage (Integer 0-100):")
    val weight = percentage()
    println("Choose the position method (single, grid):")
    val method = method(image, watermark)
    val myImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
    makeImage(image, watermark, myImage, method, weight)
    println("Input the output image filename (jpg or png extension):")
    output(myImage)
}

fun output(myImage: BufferedImage) {
    val extension = readln()
    if (!(extension.contains(".png") || extension.contains(".jpg"))) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }
    val outputFileJpg = File(extension)
    if (extension.contains(".png")) ImageIO.write(myImage, "png", outputFileJpg)
    if (extension.contains(".jpg")) ImageIO.write(myImage, "jpg", outputFileJpg)
    println("The watermarked image $extension has been created.")
}

fun makeImage(image: BufferedImage, watermark: BufferedImage, myImage: BufferedImage, method: String, weight: Int) {
    var xw = 0
    var xwg = 0
    for (x in 0 until image.width) {
        var yw = 0
        var ywg = 0
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x, y))
            var w = Color(image.getRGB(x, y), alpha)
            if (method == "grid") {
                w = Color(watermark.getRGB(xwg, ywg), alpha)
                ywg++
                if (ywg == watermark.height) ywg = 0
            }
            if (method == "single" &&
                y in posMark[1] until watermark.height + posMark[1] &&
                x in posMark[0] until watermark.width + posMark[0]
            ) {
                w = Color(watermark.getRGB(xw, yw), alpha)
                yw++
            }
            val color: Color = if (w.alpha == 0 ||
                (w.red == list[0] && w.green == list[1] && w.blue == list[2])
            )
                Color(i.red, i.green, i.blue)
            else
                Color(
                    (weight * w.red + (100 - weight) * i.red) / 100,
                    (weight * w.green + (100 - weight) * i.green) / 100,
                    (weight * w.blue + (100 - weight) * i.blue) / 100
                )
            myImage.setRGB(x, y, color.rgb)
        }
        if (x in posMark[0] until watermark.width + posMark[0]) xw++
        xwg++
        if (xwg == watermark.width) xwg = 0
    }
}

fun inputImage(): BufferedImage {
    val file = readln()
    val lines = File(file)
    if (!lines.exists()) {
        println("The file $file doesn't exist.")
        exitProcess(1)
    }
    val image = ImageIO.read(lines)
    if (image.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        exitProcess(1)
    }
    if (image.colorModel.pixelSize == 24 || image.colorModel.pixelSize == 32)
    else {
        println("The image isn't 24 or 32-bit.")
        exitProcess(1)
    }
    return image
}

fun inputWatermark(image: BufferedImage): BufferedImage {
    val fileWater = readln()
    val water = File(fileWater)
    if (!water.exists()) {
        println("The file $fileWater doesn't exist.")
        exitProcess(1)
    }
    val watermark = ImageIO.read(water)
    if (watermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        exitProcess(1)
    }
    if (watermark.colorModel.pixelSize == 24 || watermark.colorModel.pixelSize == 32)
    else {
        println("The watermark isn't 24 or 32-bit.")
        exitProcess(1)
    }
    if (image.width < watermark.width || image.height < watermark.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(1)
    }
    return watermark
}

fun method(image: BufferedImage, watermark: BufferedImage): String {
    wid = image.width - watermark.width
    hei = image.height - watermark.height
    val method = readln()
    when (method) {
        "single" -> {
            println("Input the watermark position ([x 0-$wid] [y 0-$hei]):")
            val pos = readln()
            try {
                posMark = pos.split(" ").map { a -> a.toInt() }
            } catch (e: java.lang.NumberFormatException) {
                println("The position input is invalid.")
                exitProcess(1)
            }
            if (posMark.size != 2) {
                println("The position input is invalid.")
                exitProcess(1)
            }
            if (posMark[0] !in 0..wid || posMark[1] !in 0..hei) {
                println("The position input is out of range.")
                exitProcess(1)
            }
        }
        "grid" -> {

        }
        else -> {
            println("The position method input is invalid.")
            exitProcess(1)
        }
    }
    return method
}

fun transparency(watermark: BufferedImage) {
    if (watermark.transparency == 3) {
        println("Do you want to use the watermark's Alpha channel?")
        val answer = readln()
        if (answer == "yes") alpha = true
    } else {
        println("Do you want to set a transparency color?")
        val answer = readln()
        if (answer == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            val transparency = readln()
            try {
                list = transparency.split(" ").map { a -> a.toInt() }
            } catch (e: java.lang.NumberFormatException) {
                println("The transparency color input is invalid.")
                exitProcess(1)
            }
            if (list.size != 3 ||
                list[0] !in 0..255 ||
                list[1] !in 0..255 ||
                list[2] !in 0..255
            ) {
                println("The transparency color input is invalid.")
                exitProcess(1)
            }
        }
    }
}

fun percentage(): Int {
    val weight = try {
        readln().toInt()
    } catch (e: java.lang.NumberFormatException) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(1)
    }
    if (weight < 0 || weight > 100) {
        println("The transparency percentage is out of range.")
        exitProcess(1)
    }
    return weight
}

