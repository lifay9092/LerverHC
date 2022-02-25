package cn.lifay.lerverhc.db

import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import java.io.File

object DbInfor {
    val database =
        Database.connect(
            url = "jdbc:sqlite:${System.getProperty("user.dir") + File.separator}db",
            user = "",
            password = "",
            logger = ConsoleLogger(threshold = LogLevel.DEBUG)
        )

}