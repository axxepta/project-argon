(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

declare variable $metatemplate := 'MetaTemplate.xml';
declare variable $argon_db := '~argon';

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')

let $metadb := concat('~meta_', $db)
let $meta := if(db:exists($metadb, $path)) then (
    db:open($metadb, $path)/*
) else (
    db:open($argon_db, $metatemplate)
)
(: obtain and increase revision:)
let $revision := if(not(empty($meta//revision/text()))) then (
    ($meta//revision/text()) + 1
) else (
    1
)
(: obtain and increase version:)
let $version := if(not(empty($meta//version/text()))) then (
    ($meta//version/text()) + (if ($VERSION-UP) then (1) else (0))
) else (
    1
)
(: build path for history file :)
let $date := current-date()
let $time := current-time()
let $hours := get-hours-from-time($time)
let $minutes := get-minutes-from-time($time)
let $hist-ext := concat(format-dateTime(current-dateTime(), "_[Y0001]-[M01]-[D01]_[H01]-[m01]_"), 'v', $version, 'r', $revision)

let $pathtokens := tokenize($path, '/')
let $filename := head(reverse($pathtokens))
let $histpath := if(contains($filename, '.')) then (
    let $ntokens := count($pathtokens) - 1
    let $tokenlengths := for $i in (1 to $ntokens) return string-length(subsequence($pathtokens, $i, 1))
    let $pointPos := sum($tokenlengths) + $ntokens + string-length(head(tokenize($filename, '\.'))) + 1
    return concat(substring($path, 1, $pointPos - 1), $hist-ext, substring($path, $pointPos))
) else (
    concat($path, $hist-ext)
)

return if(starts-with($RESOURCE, '<')) then (
(: first byte is angle bracket :)
let $xml := try {
    parse-xml($RESOURCE)
} catch * {
(: raise error if input is not well-formed :)
    error(xs:QName("api"), "Resource is not well-formed")
}
(: return db:add($db, $xml, $path) :)
    return db:replace($db, $path, $xml)
) else (
    db:store($db, $path, xs:base64Binary($RESOURCE))
)