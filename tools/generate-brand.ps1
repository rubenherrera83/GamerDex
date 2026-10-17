$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$res = Join-Path $root 'app/src/main/res/drawable'
$out = Join-Path $root 'docs/branding'
New-Item -ItemType Directory -Force $out | Out-Null
$g = 'M20,18 L52,18 L40,30 L28,30 L22,36 L22,64 L28,70 L40,70 L40,58 L32,58 L32,46 L52,46 L52,82 L20,82 L8,70 L8,30 Z'
$d = 'M58,18 H74 L92,36 V64 L74,82 H58 Z M70,30 V70 H71 L80,61 V39 L71,30 Z'
$glyphs = @{
 G='M8,0 H40 V9 H13 L9,13 V35 L13,39 H31 V29 H23 V20 H40 V48 H8 L0,40 V8 Z'
 A='M0,48 V8 L8,0 H32 L40,8 V48 H31 V29 H9 V48 Z M9,9 V20 H31 V9 Z'
 M='M0,48 V0 H10 L20,16 L30,0 H40 V48 H31 V16 L20,33 L9,16 V48 Z'
 E='M0,0 H40 V9 H9 V20 H32 V29 H9 V39 H40 V48 H0 Z'
 R='M0,48 V0 H32 L40,8 V22 L32,30 L42,48 H31 L21,30 H9 V48 Z M9,9 V21 H27 L31,17 V13 L27,9 Z'
 D='M0,0 H27 L40,13 V35 L27,48 H0 Z M9,9 V39 H23 L31,31 V17 L23,9 Z'
 X='M0,0 H11 L20,15 L29,0 H40 L26,24 L40,48 H29 L20,33 L11,48 H0 L14,24 Z'
}
function Save($path, $text) { [IO.File]::WriteAllText($path, $text, [Text.UTF8Encoding]::new($false)) }
function PathXml($path, $color) { '<path android:fillColor="' + $color + '" android:fillType="evenOdd" android:pathData="' + $path + '"/>' }
function Vector($w, $h, $body) {
 $intrinsicWidth = if ($w -eq 520) { '200' } else { [string]$w }
 $intrinsicHeight = if ($w -eq 520) { '38.46154' } else { [string]$h }
 '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="' + $intrinsicWidth + 'dp" android:height="' + $intrinsicHeight + 'dp" android:viewportWidth="' + $w + '" android:viewportHeight="' + $h + '">' + $body + '</vector>'
}
function Svg($w, $h, $body) { '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ' + $w + ' ' + $h + '" fill-rule="evenodd"><title>GamerDex</title>' + $body + '</svg>' }
function PathSvg($path, $color) { '<path fill="' + $color + '" d="' + $path + '"/>' }
$primary = PathXml $g '#FFFFFFFF'
$secondary = PathXml $d '#FFFFFFFF'
$svgPrimary = PathSvg $g '#F5EF26'
$svgSecondary = PathSvg $d '#00E6EF'
$letters = 'GAMERDEX'
for ($i=0; $i -lt $letters.Length; $i++) {
 $x = 124 + $i * 48
 $path = $glyphs[[string]$letters[$i]]
 $xml = '<group android:translateX="' + $x + '" android:translateY="26">' + (PathXml $path '#FFFFFFFF') + '</group>'
 $color = if ($i -lt 5) { '#F5EF26' } else { '#00E6EF' }
 $svg = '<g transform="translate(' + $x + ' 26)">' + (PathSvg $path $color) + '</g>'
 if ($i -lt 5) { $primary += $xml; $svgPrimary += $svg } else { $secondary += $xml; $svgSecondary += $svg }
}
Save "$res/brand_logo_primary.xml" (Vector 520 100 $primary)
Save "$res/brand_logo_secondary.xml" (Vector 520 100 $secondary)
$iconBody = '<group android:translateX="20" android:translateY="20" android:scaleX="0.68" android:scaleY="0.68">' + (PathXml $g '#F5EF26') + (PathXml $d '#00E6EF') + '</group>'
Save "$res/ic_gamerdex.xml" (Vector 108 108 $iconBody)
Save "$res/ic_gamerdex_monochrome.xml" (Vector 108 108 ($iconBody.Replace('#F5EF26','#FFFFFFFF').Replace('#00E6EF','#FFFFFFFF')))
Save "$res/brand_icon_background.xml" (Vector 108 108 ((PathXml 'M0,0 H108 V108 H0 Z' '#080D13') + (PathXml 'M0,22 L22,0 H26 L0,26 Z M108,82 L82,108 H86 L108,86 Z' '#1B2C38')))
Save "$out/gamerdex-logo.svg" (Svg 520 100 ($svgPrimary + $svgSecondary))
Save "$out/gamerdex-logo-mono.svg" (Svg 520 100 (($svgPrimary + $svgSecondary).Replace('#F5EF26','#FFFFFF').Replace('#00E6EF','#FFFFFF')))
Save "$out/gamerdex-symbol.svg" (Svg 100 100 ((PathSvg $g '#F5EF26') + (PathSvg $d '#00E6EF')))
Save "$out/gamerdex-icon.svg" (Svg 108 108 ('<path fill="#080D13" d="M0 0H108V108H0Z"/><g transform="translate(20 20) scale(.68)">' + (PathSvg $g '#F5EF26') + (PathSvg $d '#00E6EF') + '</g>'))
Write-Output 'Vectores Android y originales SVG generados.'
