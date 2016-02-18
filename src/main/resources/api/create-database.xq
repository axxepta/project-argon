(:~ New database name. :)
declare variable $DB as xs:string external;
declare variable $CHOP as xs:string external;
declare variable $FTINDEX as xs:string external;

let $exists := db:exists($DB)
return if(not($exists)) then (
    db:create($DB, (), (), map { 'chop' : $CHOP , 'ftindex' : $FTINDEX })
) else ()
