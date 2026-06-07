param(
    [string]$AiRefinerUrl = "http://localhost:8082/api/refine/test",
    [string]$Feeds = "https://news.google.com/rss,https://rss.cnn.com/rss/edition.rss",
    [int]$Limit = 20,
    [int]$SinceDays = 0
)

$feedsList = $Feeds -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$count = 0
$since = (Get-Date).Date.AddDays(-$SinceDays)

Write-Host "Fetching feeds: $($feedsList -join ', ')"
Write-Host "Posting to: $AiRefinerUrl"

foreach ($feed in $feedsList) {
    try {
        Write-Host "Downloading feed: $feed"
        $raw = Invoke-WebRequest -Uri $feed -UseBasicParsing -ErrorAction Stop -Headers @{ 'User-Agent' = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' }
        $xml = [xml]$raw.Content
    } catch {
        Write-Warning ("Failed to download feed " + $feed + ": " + $_)

        # Fallback: try curl.exe with SSL revocation disabled (if available)
        $curlCmd = Get-Command curl.exe -ErrorAction SilentlyContinue
        if ($curlCmd) {
            try {
                $header = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                $raw = & curl.exe --ssl-no-revoke -s -L -H $header $feed
                if ($raw -and $raw.Trim() -ne '') {
                    try {
                        $xml = [xml]$raw
                    } catch {
                        Write-Warning ("Failed to parse XML from curl for " + $feed + ": " + $_)
                        continue
                    }
                } else {
                    Write-Warning ("curl fallback returned empty for " + $feed)
                    continue
                }
            } catch {
                Write-Warning ("curl fallback failed for " + $feed + ": " + $_)
                continue
            }
        } else {
            continue
        }
    }

    $items = @()
    if ($xml.rss -ne $null) { $items = $xml.rss.channel.item }
    elseif ($xml.feed -ne $null) { $items = $xml.feed.entry }

    if ($items -eq $null) { Write-Warning ("No items parsed from " + $feed); continue }

    foreach ($item in $items) {
        if ($count -ge $Limit) { break }

        # try to extract pubDate
        $pubDate = $null
        if ($item.pubDate -ne $null) { $pubDate = [datetime]::Parse($item.pubDate) }
        elseif ($item.updated -ne $null) { $pubDate = [datetime]::Parse($item.updated) }
        elseif ($item.published -ne $null) { $pubDate = [datetime]::Parse($item.published) }
        else { $pubDate = (Get-Date) }

        if ($pubDate -lt $since) { continue }

        $title = $item.title -as [string]
        if ($title -eq $null) { $title = "(no title)" }

        # prefer content:encoded -> content -> description -> summary
        $content = $null
        if ($item.'content:encoded' -ne $null) { $content = $item.'content:encoded' }
        elseif ($item.content -ne $null) { $content = $item.content }
        elseif ($item.description -ne $null) { $content = $item.description }
        elseif ($item.summary -ne $null) { $content = $item.summary }
        else { $content = $title }

        # sanitize content: remove HTML and truncate to safe length to avoid binding issues
        $plainContent = $content -as [string]
        if ($plainContent -eq $null) { $plainContent = $title }
        $plainContent = $plainContent -replace '<[^>]+>', ''
        $plainContent = $plainContent -replace '&nbsp;',' '
        if ($plainContent.Length -gt 1200) { $plainContent = $plainContent.Substring(0,1200) }

        $news = [PSCustomObject]@{
            title = $title
            content = $plainContent
            source = $feed
        }

        $json = $news | ConvertTo-Json -Depth 10
        Write-Host "Posting item: $title"

        try {
            $resp = Invoke-RestMethod -Uri $AiRefinerUrl -Method Post -Body $json -ContentType 'application/json' -TimeoutSec 120
            Write-Host "OK -> RefinedId: $($resp.refinedId) notifyResponse: $($resp.notifyResponse)"
        } catch {
            Write-Warning "Request failed: $_"
        }

        $count++
        if ($count -ge $Limit) { break }
    }
    if ($count -ge $Limit) { break }
}

Write-Host "Done. Posted $count items."
