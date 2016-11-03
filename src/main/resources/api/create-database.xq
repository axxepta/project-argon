(:~ New database name. :)
declare variable $DB as xs:string external;
declare variable $CHOP as xs:string external;
declare variable $FTINDEX as xs:string external;
declare variable $TEXTINDEX as xs:string external;
declare variable $ATTRINDEX as xs:string external;
declare variable $TOKENINDEX as xs:string external;

let $exists := db:exists($DB)
let $meta := concat('~meta_', $DB)
let $history := concat('~history_', $DB)

return if(not($exists)) then (
    db:create($meta),
    db:create($history),
    db:create($DB, (), (), map { 'chop' : $CHOP , 'ftindex' : $FTINDEX , 'textindex' : $TEXTINDEX , 'attrindex' : $ATTRINDEX , 'tokenindex' : $TOKENINDEX })
) else ()
