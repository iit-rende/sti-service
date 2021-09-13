nel file  /opt/sti/solr-6.3.0/bin/solr.in.sh 
sono definite le proprietà "sti.index.location.NOME_INDICE" utilizzate nei file ../solrconfig.xml dei rispettivi indici per indicare la folder di destinazione dei dati
per l'indice "sti_local" va aggiunta la seguente proprietà al file /opt/sti/solr-6.3.0/bin/solr.in.sh
		SOLR_OPTS="$SOLR_OPTS -Dsti.index.location.local=/opt/sti/SOLR_IDX/LOCAL_IDX"

	