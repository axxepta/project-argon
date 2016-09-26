(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Binary storage? :)
declare variable $BINARY as xs:string external;
(:~ Original encoding for XML. :)
declare variable $ENCODING as xs:string external;
(:~ Owner of resource. :)
declare variable $OWNER as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

declare variable $metatemplate := 'MetaTemplate.xml';
declare variable $argon_db := '~argon';
declare variable $historyfile := 'historyfile';
declare variable $historyentry := 'historyentry';
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
    let $tokenized := tokenize($RESOURCE, '[<>]')
    let $n_firstTokens := min((15, count($tokenized)))
	let $firstelements := subsequence($tokenized, 1, $n_firstTokens)
	let $doctypeseq := for $j in (1 to $n_firstTokens)
		return if (contains(subsequence($firstelements, $j, 1), $DOCTYPE)) then (
			subsequence(analyze-string(subsequence($firstelements, $j, 1), '( )|".*?"')//text()[if (compare(., ' ') = 0) then () else .], 2, 5)
		) else ()
	for $doctypecomponent in $doctypeseq return <doctypecomponent>{$doctypecomponent}</doctypecomponent>
	}
	</doctype>
) else ()

(: build path for history file :)
let $timestamp := format-dateTime(current-dateTime(), "[Y0001]-[M01]-[D01]_[H01]-[m01]")
let $hist-ext := concat('_', $timestamp, '_', 'v', $version, 'r', $revision)

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

let $histuser := <historyuser>{user:current()}</historyuser>
let $histfile := <historyfile>{$histpath}</historyfile>

(: update metadata  :)
let $metaupdated := (
    $meta
) update (
    if ($VERSIONIZE) then (
        replace value of node .//version with $version,
        replace value of node .//revision with $revision,
        insert node element { $historyentry } { $histfile, $histuser } into .//history
    ) else (),
    if(not(empty($doctypetokens))) then (
        replace node .//doctype with $doctypetokens
    ) else (),
    if (empty(.//creationdate/text())) then (
        replace value of node .//creationdate with $timestamp
    ) else (),
    if (empty(.//initialencoding/text())) then (
        replace value of node .//initialencoding with $ENCODING
    ) else (),
    if (empty(.//owner/text())) then (
        replace value of node .//owner with $OWNER
    ) else (),
    replace value of node .//lastchange with $timestamp
)

let $isXML := (compare($BINARY, 'false') = 0)
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