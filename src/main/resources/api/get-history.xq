(:~ Path to resource. :)
declare variable $PATH as xs:string external;

declare variable $histpattern := '[0-9]{4}-[0-1][0-9]-[0-3][0-9]_[0-2][0-9]-[0-5][0-9]_v[0-9]+r[0-9]+';

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $metapath := concat($path, '.xml')

let $histdb := concat('argon:~history_', $db)
let $metadb := concat('~meta_', $db)
return if(db:exists($metadb, $metapath)) then (
    let $entries := db:open($metadb, $metapath)/*//historyfile/text()
    return if(empty($entries)) then (
        ()
    ) else (
        for $entry in $entries
            let $sigpos := if (matches($entry,$histpattern))
                then string-length(tokenize($entry, $histpattern)[1]) + 1
                else (0)
            let $histsignature := substring($entry, $sigpos)
            let $sigparts := tokenize($histsignature, '[v|r|.]')
            return (concat($histdb, '/', $entry), subsequence($sigparts, 2, 2), substring($histsignature, 0, 17))
    )
) else (
    ()
)