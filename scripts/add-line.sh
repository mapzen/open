for file in `find src/ res/ -type f | grep "\.xml\|\.java"`; 
do
tail -n 1 $file | grep '^$';
if [ $? = "1" ]; 
  then echo $file;
  echo "" >> $file;
fi
done
