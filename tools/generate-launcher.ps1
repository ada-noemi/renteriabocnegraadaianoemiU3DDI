# Regenerates original RelaxTV launcher artwork; no multimedia files are touched.
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path $PSScriptRoot -Parent
$background = [System.Drawing.ColorTranslator]::FromHtml('#0B132B')
$turquoise = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml('#5EEAD4'))
$wave = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml('#A4D8F3')),3.8
$wave.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$wave.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
function Draw-Emblem($graphics) {
    $moon = New-Object System.Drawing.Drawing2D.GraphicsPath
    $moon.AddBezier(65,25,44,19,31,43,46,56)
    $moon.AddBezier(46,56,58,68,76,56,77,45)
    $moon.AddBezier(77,45,62,53,49,39,65,25)
    $moon.CloseFigure()
    $graphics.FillPath($turquoise,$moon)
    $moon.Dispose()
    $graphics.DrawBezier($wave,28,69,37,61,45,77,54,69)
    $graphics.DrawBezier($wave,54,69,63,61,71,61,80,69)
    $graphics.DrawBezier($wave,31,79,39,72,46,86,54,79)
    $graphics.DrawBezier($wave,54,79,62,72,69,72,77,79)
}
try {
    $densities = @{'mdpi'=48;'hdpi'=72;'xhdpi'=96;'xxhdpi'=144;'xxxhdpi'=192}
    foreach ($density in $densities.Keys) {
        $size = $densities[$density]
        $bitmap = New-Object System.Drawing.Bitmap $size,$size
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
            $graphics.Clear($background)
            $graphics.ScaleTransform(($size / 108.0),($size / 108.0))
            Draw-Emblem $graphics
            $path = Join-Path $projectRoot "app/src/main/res/mipmap-$density/ic_launcher.png"
            $bitmap.Save($path,[System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $graphics.Dispose(); $bitmap.Dispose() }
    }
    $banner = New-Object System.Drawing.Bitmap 320,180
    $graphics = [System.Drawing.Graphics]::FromImage($banner)
    $font = New-Object System.Drawing.Font 'Segoe UI',30,([System.Drawing.FontStyle]::Bold),([System.Drawing.GraphicsUnit]::Pixel)
    $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml('#EDF6FF'))
    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
        $graphics.Clear($background)
        $graphics.TranslateTransform(2,12)
        $graphics.ScaleTransform(1.35,1.35)
        Draw-Emblem $graphics
        $graphics.ResetTransform()
        $graphics.DrawString('RelaxTV',$font,$textBrush,139,73)
        $banner.Save((Join-Path $projectRoot 'app/src/main/res/drawable-xhdpi/tv_banner.png'),[System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $font.Dispose(); $textBrush.Dispose(); $graphics.Dispose(); $banner.Dispose() }
} finally { $turquoise.Dispose(); $wave.Dispose() }
