@echo off
REM ============================================================================
REM 下载官方 MCP Filesystem Server
REM ============================================================================

echo 正在下载 MCP Filesystem Server 源码...

REM 创建临时目录
if not exist "temp-mcp" mkdir temp-mcp
cd temp-mcp

REM 下载官方 filesystem server
git clone --depth 1 --filter=blob:none --sparse https://github.com/modelcontextprotocol/servers.git
cd servers
git sparse-checkout set src/filesystem

REM 打包为 zip
echo 正在打包为 zip...
cd src/filesystem
powershell Compress-Archive -Path * -DestinationPath ../../../filesystem-mcp.zip -Force

REM 清理临时文件
cd ../../..
rd /s /q servers

echo.
echo ========================================
echo 下载完成！
echo ZIP 包位置: %CD%\filesystem-mcp.zip
echo ========================================
echo.
pause
