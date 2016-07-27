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
declare variable $historyfile := 'historyfile';
declare variable $DOCTYPE := '!DOCTYPE';

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $metapath := concat($path, '.xml')

let $histdb := concat('~history_', $db)
let $metadb := concat('~meta_', $db)
let $meta := if(db:exists($metadb, $metapath)) then (
    db:open($metadb, $metapath)/*
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

(: get doctype definition :)
let $doctypetokens := if(contains($RESOURCE, $DOCTYPE)) then (
    <doctype>
    {
	let $firstelements := subsequence(tokenize($RESOURCE, '[<>]'), 1, 4)
	let $doctypeseq := for $j in (1 to 4)
		return if (contains(subsequence($firstelements, $j, 1), $DOCTYPE)) then (
			subsequence(analyze-string(subsequence($firstelements, $j, 1), '( )|".*?"')//text()[if (compare(., ' ') = 0) then () else .], 2, 5)
		) else ()
	for $doctypecomponent in $doctypeseq return <doctypecomponent>{$doctypecomponent}</doctypecomponent>
	}
	</doctype>
) else ()

(: build path for history file :)
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

(: update metadata  :)
let $metaupdated := (
    $meta
) update (
    if ($VERSIONIZE) then (
        replace value of node .//version with $version,
        replace value of node .//revision with $revision,
        insert node element { $historyfile } { $histpath } into .//history
    ) else (),
    if(not(empty($doctypetokens))) then (
        replace node .//doctype with $doctypetokens
    ) else ()
)

let $isXML := starts-with($RESOURCE, '<') and not(ends-with($PATH, '.html') or ends-with($PATH, '.htm'))
let $xml := if($isXML) then (
    try {
        parse-xml($RESOURCE)
    } catch * {
    (: raise error if input is not well-formed :)
        error(xs:QName("api"), "Resource is not well-formed")
    }
) else ('')

return if($isXML) then (
    db:replace($db, $path, $RESOURCE),
    db:replace($metadb, $metapath, $metaupdated),
    if($VERSIONIZE) then (db:replace($histdb, $histpath, $RESOURCE)) else ()
) else (
    db:store($db, $path, xs:base64Binary($RESOURCE)),
    db:replace($metadb, $metapath, $metaupdated),
    if($VERSIONIZE) then (db:store($histdb, $histpath, xs:base64Binary($RESOURCE))) else ()
)