import api.ServiceApi.Companion.apiGetToken
import api.ServiceApi.Companion.apiQueryData
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.xenomachina.argparser.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.TokenHolder


private val logger = KotlinLogging.logger {}


class ParsedArgs(parser: ArgParser) {

    val validURL = "^(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)(/)\$"
    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "valide le mode verbeux"
    )

    val server by parser.storing(
        "-s", "--server",
        help = "URL API du serveur Discovery , (https et termine avec '/') \n " +
        "généralement https://server/api/v1.1/"
    )
//        .addValidator {
//        if (!value.matches(Regex(validURL)))
//            throw InvalidArgumentException("URL du serveur invalide : $value \n" +
//                "lancer le programme avec l'option -h pour de l'aide")
//    }

    val unsafe by parser.flagging(
        "-x", "--unsecure",
        help = "do not verify SSL certificate checking process (useful with self signed certificate)"
    ).default(false)

    val username by parser.storing(
        "-u", "--username",
        help = "Login - Nom de l'utilisateur"
    )

    val password by parser.storing(
        "-p", "--password",
        help = "Login - Mot de passe"
    )

    val query by parser.storing(
        "-q", "--query",
        help = "requete sur le serveur (par défaut : search Host)"
    ).default("search Host")


    val output by parser.storing(
        "-o", "--output",
        help = "Chemin complet du fichier resultat (par défaut : DiscoveryExtractedData.csv) "
    ).default("DiscoveryExtractedData.csv")

//    val debugLevel by parser.mapping(
//        "--info" to Mode.INFO,
//        "--debug" to Mode.DEBUG,
//        "--error" to Mode.ERROR,
//        help = "niveau de logging")
}

    fun main(args: Array<String>) = mainBody {



        val prologue = "Discovery Extractor : Execute une query de recherche sur le serveur et enregistre les resultats dans un fichier CSV"
        val epilogue = "TSODev pour Orange Business"

        ArgParser(args,ArgParser.Mode.GNU,DefaultHelpFormatter(prologue,epilogue)).parseInto(::ParsedArgs).run {


            logger.info("===========================================================================")
            logger.info(" Discovery Data Extractor - TSO pour Orange Business - 06/23 - version 1.0 ")
            logger.info("===========================================================================")

            val csv_File_Path = output

            val csvWriter = csvWriter {
                charset = "ISO_8859_1"
                delimiter = ';'
                nullCode = ""
                lineTerminator = "\r\n"
                outputLastLineTerminator = true
                quote {
                    mode = WriteQuoteMode.CANONICAL
                    char = '"'
                }
            }

            if (verbose) logger.info("Exécution de [ $query ] sur $server")
            else logger.debug("Exécution de [ $query ] sur $server")

            apiCallByCoroutines(
                username,
                password,
                server,
                query,
                verbose,
                unsafe,
                csvWriter,
                csv_File_Path
            )
            if (verbose) logger.info("Fichier [ $output ] créé")
            else logger.debug("Fichier [ $output ] créé")

        }
    }

    private fun apiCallByCoroutines(
        username: String,
        password: String,
        server: String,
        query: String,
        verbose: Boolean,
        unsafe: Boolean,
        csvWriter: CsvWriter,
        CSV_File_Path: String
    ) = runBlocking {
        launch { // launch new coroutine in the scope of runBlocking

            try {

                if (username.isNotEmpty() && password.isNotEmpty())
                    apiGetToken(server, username, password, unsafe)?.let { token ->
                        TokenHolder.saveToken(token)
                    }

//            val csvData = mutableListOf(listOf(""))
                apiQueryData(server, query, verbose, unsafe)?.let { results ->
                    csvWriter.openAsync(CSV_File_Path) {
                        writeRow(results.first().headings)
                        results.forEach { rows ->
                            rows.results.forEach { row ->
                                writeRow(row)
                            }
                        }
                    }
                }
            }
            catch (exception: Exception) {
                logger.error(exception){"Erreur : $exception -> vérifiez les arguments svp..."}
            }
        }
    }
