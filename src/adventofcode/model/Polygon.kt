package adventofcode.model

import kotlin.math.absoluteValue

@Suppress("MemberVisibilityCanBePrivate")
class Polygon(private val vertices: List<Point>) {

    // Calculate the area of the Polygon including the perimeter using Pick's theorem
    // https://en.wikipedia.org/wiki/Pick%27s_theorem
    fun areaWithPerimeter(): Long {
        // Pick's theorem is A = i + b/2 - 1
        // A is the area, i the number of points inside the polygon, and b the number of points outside the polygon.
        // In this case A is already known, so the formula can be changed to i + b = A + b/2 + 1
        return area() + perimeter() / 2 + 1
    }

    // Calculate the area of a Polygon using the shoelace formula
    // https://en.wikipedia.org/wiki/Shoelace_formula
    fun area(): Long {
        var area = 0L
        var j = vertices.lastIndex
        for (i in vertices.indices) {
            area += (vertices[j].x + vertices[i].x).toLong() * (vertices[j].y - vertices[i].y)
            j = i
        }
        return (area / 2).absoluteValue
    }

    // Calculate the distance by taking the sum of the distance between the vertices
    fun perimeter(): Long {
        var perimeter = 0L

        var j = vertices.lastIndex
        for (i in vertices.indices) {
            perimeter += vertices[j] distanceTo vertices[i]
            j = i
        }

        return perimeter
    }
}