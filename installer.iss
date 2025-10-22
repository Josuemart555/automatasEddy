; Inno Setup Script (plantilla) para crear instalador de la app AFD
; Requiere instalar Inno Setup: https://jrsoftware.org/isinfo.php

[Setup]
AppName=Simulador AFD
AppVersion=1.0
DefaultDirName={pf64}\SimuladorAFD
DefaultGroupName=Simulador AFD
OutputDir=.
OutputBaseFilename=SimuladorAFD-Setup
Compression=lzma
SolidCompression=yes

[Files]
; Antes de compilar el instalador, genere un .jar ejecutable de la app (desde IntelliJ: Build Artifacts)
; y actualice la ruta abajo con su archivo .jar y recursos si los hubiera.
Source: "dist\SimuladorAFD.jar"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\Simulador AFD"; Filename: "{app}\run.bat"; WorkingDir: "{app}"
Name: "{commondesktop}\Simulador AFD"; Filename: "{app}\run.bat"; Tasks: desktopicon

[Tasks]
Name: desktopicon; Description: "Crear icono en el escritorio"; GroupDescription: "Opciones adicionales:"; Flags: unchecked

[Run]
Filename: "{app}\\run.bat"; Description: "Iniciar Simulador AFD"; Flags: nowait postinstall skipifsilent

[Code]
; Crea un run.bat para ejecutar el jar con doble clic
procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssInstall then begin
    SaveStringToFile(ExpandConstant('{app}') + '\\run.bat', '"%JAVA_HOME%\\bin\\java" -jar "%~dp0SimuladorAFD.jar"', False);
  end;
end
