@Echo off&setlocal,EnableDelayedExpansion


@REM
@REM set MAVEN_HOME=D:\apache-maven-3.8.4

@REM set JAVA_HOME=D:\Program Files\Java\jdk1.8.0_311
@REM set JAVA_HOME=D:\Program Files\Java\graalvm-ee-java17-22.0.0.2
@REM set PATH=%JAVA_HOME%/bin;%JAVA_HOME%/jre/bin;%MAVEN_HOME%/bin;
@REM set CLASSPATH=.;%JAVA_HOME%/lib/dt.jar;%JAVA_HOME%/lib/tools.jar;ar;lib/run.jar

@REM java -version

mvn gluonfx:runagent --settings D:\apache-maven-3.2.5\conf\settings-git.xml -Dmaven.repo.local=D:\apache-maven-3.2.5\git


:Choice
set /p Choice=请选择(Y/N):
IF /i "!Choice!"=="Y" Goto :Next
IF /i "!Choice!"=="N" Goto :End
Echo 您输入的!Choice!不合法！，请按任意键返回重新输入。
Pause>Nul&Goto :Choice
:Next
Echo 继续执行...

mvn gluonfx:build --settings D:\apache-maven-3.2.5\conf\settings-git.xml -Dmaven.repo.local=D:\apache-maven-3.2.5\git

mvn gluonfx:runagent --settings D:\apache-maven-3.2.5\conf\settings-git.xml -Dmaven.repo.local=D:\apache-maven-3.2.5\git

mvn gluonfx:package --settings D:\apache-maven-3.2.5\conf\settings-git.xml -Dmaven.repo.local=D:\apache-maven-3.2.5\git

mvn gluonfx:nativerun --settings D:\apache-maven-3.2.5\conf\settings-git.xml -Dmaven.repo.local=D:\apache-maven-3.2.5\git

pause&exit
:End
Echo 退出...&pause&exit



