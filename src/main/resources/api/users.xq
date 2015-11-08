(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

distinct-values(
        if(not(db:exists($LOCK-DB))) then () else
            db:open($LOCK-DB)/*/@user/string()
)