package io.github.rsookram.soon.data

import androidx.datastore.core.Serializer
import io.github.rsookram.soon.Data
import java.io.InputStream
import java.io.OutputStream

class DataSerializer : Serializer<Data> {

    override val defaultValue = Data()

    // TODO: Check which thread these methods are called on
    override suspend fun readFrom(input: InputStream): Data =
        Data.ADAPTER.decode(input)

    override suspend fun writeTo(t: Data, output: OutputStream) {
        Data.ADAPTER.encode(output, t)
    }
}
