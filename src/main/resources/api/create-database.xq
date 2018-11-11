(:~ New database name. :)
declare variable $DB as xs:string external;
declare variable $OPT_CHOP as xs:string external;
declare variable $OPT_FTINDEX as xs:string external;
declare variable $OPT_TEXTINDEX as xs:string external;
declare variable $OPT_ATTRINDEX as xs:string external;
declare variable $OPT_TOKENINDEX as xs:string external;

let $exists := db:exists($DB)
let $meta := concat('~meta_', $DB)
let $history := concat('~history_', $DB)

return if(not($exists)) then (
    db:create($meta),
    db:create($history),
    db:create($DB, (), (), map { 'chop' : $OPT_CHOP , 'ftindex' : $OPT_FTINDEX , 'textindex' : $OPT_TEXTINDEX , 'attrindex' : $OPT_ATTRINDEX , 'tokenindex' : $OPT_TOKENINDEX })
) else ()
