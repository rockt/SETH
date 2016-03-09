#Very simple script to convert a local MySQL database to a more portable Derby version
#We implemented this script, because the migration process was not managable using ddlutils for larger dbSNP databases

user=username
pass=passphrase
dbase=database

#Export mysql to CSV-file
mysql -u $user -p$pass $dbase -e "SELECT * FROM PSM INTO OUTFILE '/tmp/PSM.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/PSM.tsv .

mysql -u $user -p$pass $dbase -e "SELECT * FROM gene2pubmed INTO OUTFILE '/tmp/gene2pubmed.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/gene2pubmed.tsv .

mysql -u $user -p$pass $dbase -e "SELECT * FROM genes INTO OUTFILE '/tmp/genes.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/genes.tsv .

mysql -u $user -p$pass $dbase -e "SELECT * FROM hgvs INTO OUTFILE '/tmp/hgvs.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/hgvs.tsv .

mysql -u $user -p$pass $dbase -e "SELECT * FROM seth_transcripts INTO OUTFILE '/tmp/transcripts.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/transcripts.tsv .

mysql -u $user -p$pass $dbase -e "SELECT * FROM uniprot INTO OUTFILE '/tmp/uniprot.tsv' FIELDS TERMINATED BY ';' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\n'  ;"
sudo mv /tmp/uniprot.tsv .


#start derby
derby-src/db-derby-10.12.1.1-bin/bin/ij

-- Create database
CONNECT 'jdbc:derby:dataFolder/dbName;create=true';
-- Create tables
run 'resources/table.sql';

-- Import data
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','PSM','dataFolder/PSM.tsv',';','"','UTF-8',1);
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','GENE2PUBMED','dataFolder/gene2pubmed.tsv',';','"','UTF-8',1);
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','GENES','dataFolder/genes.tsv',';','"','UTF-8',1);
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','HGVS','dataFolder/hgvs.tsv',';','"','UTF-8',1);
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','UNIPROT','dataFolder/uniprot.tsv',';','"','UTF-8',1);
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP','SETH_TRANSCRIPTS','dataFolder/transcripts.tsv',';','"','UTF-8',1);

