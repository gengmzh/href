#!/bin/sh

JAVA='java'
CRAWLER_HOME=/home/mp3/gengmzh/openfy/crawler
CRAWLER_JAR=$CRAWLER_HOME/zhiyu-crawler-0.2.0-jar-with-dependencies.jar
MAIN_CLASS=cn.seddat.zhiyu.crawler.service.CrawlerService
LOG_CONFIG=$CRAWLER_HOME/logging.properties
LOG_FILE=$CRAWLER_HOME/logs/crawler.log
echo 'jar '$JOB_JAR
echo 'main class '$MAIN_CLASS
echo 'log config '$LOG_CONFIG
echo 'log file '$LOG_FILE

# killing old crawler
PID=`ps aux |grep "$MAIN_CLASS" |grep -v "grep" |awk 'BEGIN{FS=" ";} {print $2;}'`
if [ ! -z $PID ]; then
        echo "killing crawler $PID" >>$LOG_FILE
        kill $PID && sleep 5
        PID=`ps aux |grep "$MAIN_CLASS" |grep -v "grep" |awk 'BEGIN{FS=" ";} {print $2;}'`
        if [ ! -z $PID ]; then
                echo "killing crawler $PID immediately" >>$LOG_FILE
                kill -9 $PID
        fi
fi

# starting new crawler
echo 'starting crawler...'
nohup $JAVA -cp $CRAWLER_JAR -Djava.util.logging.config.file=$LOG_CONFIG $MAIN_CLASS >>$LOG_FILE 2>&1 &
echo 'crawler started'

exit 0
