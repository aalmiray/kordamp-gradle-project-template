import uk.co.cacoethes.util.NameType

Map props = [:]
def className = ''
if (projectDir.name =~ /\-/) {
    className = transformText(projectDir.name, from: NameType.HYPHENATED, to: NameType.CAMEL_CASE)
} else {
    className = transformText(projectDir.name, from: NameType.PROPERTY, to: NameType.CAMEL_CASE)
}
props.project_name = transformText(className, from: NameType.CAMEL_CASE, to: NameType.HYPHENATED)
props.project_author = System.properties['user.name']

def projectType
def projectTypes = ['generic', 'java', 'groovy', 'kotlin', 'scala']
println '\nThe following project types are available\n'
projectTypes.each { println "  $it" }
println ' '
while (!(projectType in projectTypes)) {
    projectType = ask("Which type of project do you want to generate? ", null, 'type')?.toLowerCase()
}

def fileExtension = projectType == 'generic' ? 'java' : projectType
def nonJavaProject = projectType != 'generic' && projectType != 'java'
projectType = projectType == 'generic' ? '' : projectType + '-'

props.project_group = ask("Define value for 'group' [org.example]: ", 'org.example', 'group')
props.project_name = ask("Define value for 'artifactId' [" + props.project_name + "]: ", props.project_name, 'artifactId')
props.project_version = ask("Define value for 'version' [0.0.0-SNAPSHOT]: ", '0.0.0-SNAPSHOT', 'version')
props.kordamp_version = ask("Define value for 'kordampVersion' [0.30.4]: ", '0.30.4', 'kordampVersion')
props.project_layout = ask("Define value for 'layout' [two-level] from (single, standard, two-level, multi-level): ", 'two-level', 'layout')
props.project_author = ask("Define valud for 'author' [" + props.project_author + "]: ", props.project_author, 'author')
props.project_license = ask("Define value for 'license' [Apache-2.0]: ", 'Apache-2.0', 'license')

processTemplates 'gradle.properties', props

def createGuide = { File dir ->
    dir.mkdirs()
    String buildFileName = dir.name + '.gradle'
    new File(dir, buildFileName).text = """
    |plugins {
    |    id 'org.kordamp.gradle.guide' version '${props.kordamp_version}'
    |}
    |""".stripMargin()
}

def createSubproject = { File dir ->
    dir.mkdirs()
    String buildFileName = dir.name + '.gradle'
    new File(dir, buildFileName).text = """
    |plugins {
    |    id 'java-library'${nonJavaProject?"\n    id '${fileExtension}'":""}
    |}
    |
    |config {
    |    info {
    |        name        = '${dir.name}'
    |        description = 'Description of ${dir.name}'
    |    }
    |}
    |""".stripMargin()
    new File(dir, 'src/main/' + fileExtension).mkdirs()
    new File(dir, 'src/test/' + fileExtension).mkdirs()
}

def currentYear = { ->
    Date now = new Date()
    Calendar c = Calendar.getInstance()
    c.setTime(now)
    return c.get(Calendar.YEAR).toString()
}

def createBuildfileSingle = { ->
    new File(projectDir, 'build.gradle').text = """
    |plugins {
    |    id 'java-library'${nonJavaProject?"\n    id '${fileExtension}'":""}
    |    id 'org.kordamp.gradle.${projectType}project' version '${props.kordamp_version}'
    |}
    |
    |config {
    |    release = (project.rootProject.findProperty('release') ?: false).toBoolean()
    |
    |    info {
    |        name          = '${props.project_name}'
    |        description   = 'Description of ${props.project_name}'
    |        vendor        = '${props.project_author}'
    |        inceptionYear = '${currentYear()}'
    |
    |        links {
    |            website      = "https://github.com/${props.project_author}/\${project.rootProject.name}"
    |            issueTracker = "https://github.com/${props.project_author}/\${project.rootProject.name}/issues"
    |            scm          = "https://github.com/${props.project_author}/\${project.rootProject.name}.git"
    |        }
    |
    |        people {
    |            person {
    |                id    = '${props.project_author}'
    |                name  = '${props.project_author}'
    |                roles = ['developer', 'author']
    |            }
    |        }
    |    }
    |
    |    licensing {
    |        licenses {
    |            license {
    |                id = '${props.project_license}'
    |            }
    |        }
    |    }
    |
    |    javadoc {
    |        excludes = ['**/*.html', 'META-INF/**']
    |    }
    |}
    |
    |repositories {
    |    jcenter()
    |    mavenCentral()
    |}
    |
    |normalization {
    |    runtimeClasspath {
    |        ignore('/META-INF/MANIFEST.MF')
    |    }
    |}
    |""".stripMargin()
}

def createBuildfileMulti = { ->
    new File(projectDir, 'build.gradle').text = """
    |plugins {
    |    id 'org.kordamp.gradle.${projectType}project' version '${props.kordamp_version}'
    |}
    |
    |config {
    |    release = (project.rootProject.findProperty('release') ?: false).toBoolean()
    |
    |    info {
    |        name          = '${props.project_name}'
    |        description   = 'Description of ${props.project_name}'
    |        vendor        = '${props.project_author}'
    |        inceptionYear = '${currentYear()}'
    |
    |        links {
    |            website      = "https://github.com/${props.project_author}/\${project.rootProject.name}"
    |            issueTracker = "https://github.com/${props.project_author}/\${project.rootProject.name}/issues"
    |            scm          = "https://github.com/${props.project_author}/\${project.rootProject.name}.git"
    |        }
    |
    |        people {
    |            person {
    |                id    = '${props.project_author}'
    |                name  = '${props.project_author}'
    |                roles = ['developer', 'author']
    |            }
    |        }
    |    }
    |
    |    licensing {
    |        licenses {
    |            license {
    |                id = '${props.project_license}'
    |            }
    |        }
    |    }
    |
    |    javadoc {
    |        excludes = ['**/*.html', 'META-INF/**']
    |    }
    |}
    |
    |allprojects {
    |    repositories {
    |        jcenter()
    |        mavenCentral()
    |    }
    |
    |    normalization {
    |        runtimeClasspath {
    |            ignore('/META-INF/MANIFEST.MF')
    |        }
    |    }
    |}
    |""".stripMargin()
}

def createSettingsSingle = { ->
    new File(projectDir, 'settings.gradle').text = """
    |pluginManagement {
    |    repositories {
    |        jcenter()
    |        gradlePluginPortal()
    |    }
    |}
    |
    |rootProject.name = '${props.project_name}'
    |""".stripMargin()
}


def createSettingsMulti = { dirs ->
    String directories = ''
    if (dirs) {
        directories = """\n    directories = [\n        ${dirs.collect({ d -> "'${d}'" }).join(',\n        ')}\n    ]"""
    }

    new File(projectDir, 'settings.gradle').text = """
    |pluginManagement {
    |    repositories {
    |        jcenter()
    |        gradlePluginPortal()
    |    }
    |}
    |
    |buildscript {
    |    repositories {
    |        gradlePluginPortal()
    |    }
    |    dependencies {
    |        classpath 'org.kordamp.gradle:settings-gradle-plugin:${props.kordamp_version}'
    |    }
    |}
    |apply plugin: 'org.kordamp.gradle.settings'
    |
    |rootProject.name = '${props.project_name}'
    |
    |projects {
    |    layout = '${props.project_layout}'${directories}
    |}
    |""".stripMargin()
}

switch(props.project_layout.toLowerCase()) {
    case 'i':
    case 'single':
        new File(projectDir, 'src/main/' + fileExtension).mkdirs()
        new File(projectDir, 'src/test/' + fileExtension).mkdirs()
        createBuildfileSingle()
        createSettingsSingle()
        break
    case 's':
    case 'standard':
        createGuide(new File(projectDir, 'guide'))
        createSubproject(new File(projectDir, props.project_name + '-core'))
        createBuildfileMulti()
        createSettingsMulti()
        break
    case 'm':
    case 'multi-level':
        createGuide(new File(projectDir, 'guide'))
        createSubproject(new File(projectDir, 'subprojects/' + props.project_name + '-core'))
        createBuildfileMulti()
        createSettingsMulti(['guide', 'subprojects/' + props.project_name + '-core'])
        break
    case 't':
    case 'two-level':
    default:
        createGuide(new File(projectDir, 'docs/guide'))
        createSubproject(new File(projectDir, 'subprojects/' + props.project_name + '-core'))
        createBuildfileMulti()
        createSettingsMulti()
        break
}
