#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE userdb;
    CREATE DATABASE accountdb;
    CREATE DATABASE transactiondb;
    CREATE DATABASE loandb;
    CREATE DATABASE notificationdb;
EOSQL
