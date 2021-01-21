package org.jetbrains.compose.desktop.application.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.internal.*
import java.io.File
import javax.inject.Inject

abstract class AbstractJPackageTask @Inject constructor(
    @get:Input
    val targetFormat: TargetFormat,
) : AbstractJvmToolOperationTask("jpackage") {
    @get:InputFiles
    val files: ConfigurableFileCollection = objects.fileCollection()

    @get:InputDirectory
    @get:Optional
    /** @see internal/wixToolset.kt */
    val wixToolsetDir: DirectoryProperty = objects.directoryProperty()

    @get:Input
    @get:Optional
    val installationPath: Property<String?> = objects.nullableProperty()

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val licenseFile: RegularFileProperty = objects.fileProperty()

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val iconFile: RegularFileProperty = objects.fileProperty()

    @get:Input
    val launcherMainClass: Property<String> = objects.notNullProperty()

    @get:InputFile
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val launcherMainJar: RegularFileProperty = objects.fileProperty()

    @get:Input
    @get:Optional
    val launcherArgs: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    @get:Optional
    val launcherJvmArgs: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    val packageName: Property<String> = objects.notNullProperty()

    @get:Input
    @get:Optional
    val packageDescription: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val packageCopyright: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val packageVendor: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val packageVersion: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxShortcut: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxPackageName: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxAppRelease: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxAppCategory: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxDebMaintainer: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxMenuGroup: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val linuxRpmLicenseType: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val macPackageIdentifier: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val macPackageName: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val macBundleSigningPrefix: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val macSign: Property<Boolean?> = objects.nullableProperty()

    @get:InputFile
    @get:Optional
    val macSigningKeychain: RegularFileProperty = objects.fileProperty()

    @get:Input
    @get:Optional
    val macSigningKeyUserName: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winConsole: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winDirChooser: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winPerUserInstall: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winShortcut: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winMenu: Property<Boolean?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winMenuGroup: Property<String?> = objects.nullableProperty()

    @get:Input
    @get:Optional
    val winUpgradeUuid: Property<String?> = objects.nullableProperty()

    @get:InputDirectory
    @get:Optional
    val runtimeImage: DirectoryProperty = objects.directoryProperty()

    override fun makeArgs(tmpDir: File): MutableList<String> = super.makeArgs(tmpDir).apply {
        cliArg("--input", tmpDir)
        cliArg("--type", targetFormat.id)

        cliArg("--dest", destinationDir.ioFile)
        cliArg("--verbose", verbose)

        if (targetFormat != TargetFormat.AppImage) {
            cliArg("--install-dir", installationPath)
            cliArg("--license-file", licenseFile.ioFileOrNull)
        }
        cliArg("--icon", iconFile.ioFileOrNull)

        cliArg("--name", packageName)
        cliArg("--description", packageDescription)
        cliArg("--copyright", packageCopyright)
        cliArg("--app-version", packageVersion)
        cliArg("--vendor", packageVendor)

        cliArg("--main-jar", launcherMainJar.ioFile.name)
        cliArg("--main-class", launcherMainClass)
        launcherArgs.orNull?.forEach {
            cliArg("--arguments", it)
        }
        launcherJvmArgs.orNull?.forEach {
            cliArg("--java-options", it)
        }

        when (currentOS) {
            OS.Linux -> {
                if (targetFormat != TargetFormat.AppImage) {
                    cliArg("--linux-shortcut", linuxShortcut)
                    cliArg("--linux-package-name", linuxPackageName)
                    cliArg("--linux-app-release", linuxAppRelease)
                    cliArg("--linux-app-category", linuxAppCategory)
                    cliArg("--linux-deb-maintainer", linuxDebMaintainer)
                    cliArg("--linux-menu-group", linuxMenuGroup)
                    cliArg("--linux-rpm-license-type", linuxRpmLicenseType)
                }
            }
            OS.MacOS -> {
                cliArg("--mac-package-identifier", macPackageIdentifier)
                cliArg("--mac-package-name", macPackageName)
                cliArg("--mac-bundle-signing-prefix", macBundleSigningPrefix)
                cliArg("--mac-sign", macSign)
                cliArg("--mac-signing-keychain", macSigningKeychain.ioFileOrNull)
                cliArg("--mac-signing-key-user-name", macSigningKeyUserName)
            }
            OS.Windows -> {
                cliArg("--win-console", winConsole)
                if (targetFormat != TargetFormat.AppImage) {
                    cliArg("--win-dir-chooser", winDirChooser)
                    cliArg("--win-per-user-install", winPerUserInstall)
                    cliArg("--win-shortcut", winShortcut)
                    cliArg("--win-menu", winMenu)
                    cliArg("--win-menu-group", winMenuGroup)
                    cliArg("--win-upgrade-uuid", winUpgradeUuid)
                }
            }
        }

        cliArg("--runtime-image", runtimeImage)
    }

    override fun prepareWorkingDir(inputChanges: InputChanges) {
        val workingDir = workingDir.ioFile

        if (inputChanges.isIncremental) {
            logger.debug("Updating working dir incrementally: $workingDir")
            val allChanges = inputChanges.getFileChanges(files).asSequence() +
                    inputChanges.getFileChanges(launcherMainJar)
            allChanges.forEach { fileChange ->
                val sourceFile = fileChange.file
                val targetFile = workingDir.resolve(sourceFile.name)

                if (fileChange.changeType == ChangeType.REMOVED) {
                    fileOperations.delete(targetFile)
                    logger.debug("Deleted: $targetFile")
                } else {
                    sourceFile.copyTo(targetFile, overwrite = true)
                    logger.debug("Updated: $targetFile")
                }
            }
        } else {
            logger.debug("Updating working dir non-incrementally: $workingDir")
            fileOperations.delete(workingDir)
            fileOperations.mkdir(workingDir)
            fileOperations.copy {
                it.from(files)
                it.from(launcherMainJar)
                it.into(workingDir)
            }
        }
    }

    override fun configureExec(exec: ExecSpec) {
        super.configureExec(exec)
        configureWixPathIfNeeded(exec)
    }

    private fun configureWixPathIfNeeded(exec: ExecSpec) {
        if (currentOS == OS.Windows) {
            val wixDir = wixToolsetDir.ioFileOrNull ?: return
            val wixPath = wixDir.absolutePath
            val path = System.getenv("PATH") ?: ""
            exec.environment("PATH", "$wixPath;$path")
        }
    }

    override fun checkResult(result: ExecResult) {
        super.checkResult(result)

        val finalLocation = destinationDir.ioFile.let { destinationDir ->
            when (targetFormat) {
                TargetFormat.AppImage -> destinationDir
                else -> destinationDir.walk().first { it.isFile && it.name.endsWith(targetFormat.fileExt) }
            }
        }
        logger.lifecycle("The distribution is written to ${finalLocation.canonicalPath}")
    }
}