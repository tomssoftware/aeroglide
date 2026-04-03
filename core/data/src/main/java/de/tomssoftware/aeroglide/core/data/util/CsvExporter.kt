package de.tomssoftware.aeroglide.core.data.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringWriter
import kotlin.reflect.full.memberProperties

/**
 * A smart, reusable CSV exporter that can serialize a list of any data class.
 * It uses reflection to automatically generate headers and record values.
 */
object CsvExporter {

    /**
     * Builds a CSV string from a list of items of any data class type.
     *
     * @param T The type of the data class in the list (e.g., Location, Altitude).
     * @param data The list of items to be exported.
     * @return A String containing the full CSV content, including the header.
     */
    inline fun <reified T : Any> buildCsv(data: List<T>): String {
        // Use StringWriter as a more standard way to build the string for CSVPrinter
        val stringWriter = StringWriter()

        // 1. Get the properties of the data class `T` using reflection.
        // This gives us an ordered list of properties like 'timestamp', 'altitude', etc.
        val properties = T::class.memberProperties
            .sortedBy { it.name } // Sort alphabetically for consistent header order

        // 2. Automatically generate the header from the property names.
        val header = properties.map { it.name }.toTypedArray()

        val csvFormat = CSVFormat.Builder.create()
            .setHeader(*header) // Use the generated header
            .get()

        // 3. Use the CSVPrinter to write records
        val csvPrinter = CSVPrinter(stringWriter, csvFormat)

        csvPrinter.use { printer ->
            // Iterate over each item in the data list (e.g., each Location object)
            data.forEach { item ->
                // For each item, get its values in the same order as the header
                val record = properties.map { prop ->
                    // prop.get(item) uses reflection to get the value of a property
                    // from the current item, e.g., gets the value of 'altitude' from a 'Location' instance.
                    prop.get(item)
                }
                printer.printRecord(record)
            }
        } // .use { ... } automatically flushes and closes the printer

        return stringWriter.toString()
    }
}
