(:~ Source. :)
declare variable $RESOURCE as xs:string external;

declare variable $LOCK-DB := '~argon';
declare variable $PATH := 'MetaTemplate.xml';

let $exists := db:exists($LOCK-DB)
return if($exists) then (
    db:replace($LOCK-DB, $PATH, $RESOURCE)
) else (
    db:create($LOCK-DB, $RESOURCE, $PATH)
)