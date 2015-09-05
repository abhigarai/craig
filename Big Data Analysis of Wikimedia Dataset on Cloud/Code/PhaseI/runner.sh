#! /bin/bash

######################################################################
# Answer script for Project 1 module 1 Fill in the functions below ###
# for each question. You may use any other files/scripts/languages ###
# in these functions as long as they are in the submission folder. ###
######################################################################

# Write or invoke the code to perform filtering on the dataset. Redirect 
# the filtered output to a file called 'output' in the current folder.
answer_1() {
	# Fill in this Bash function to filter the dataset and redirect the 
	# output to a file called 'output'.
        javac Project.java
        java Project > output
}

# How many lines (items) were originally present in the input file 
# pagecounts-20141101-000000 i.e line count before filtering
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_2() {
	# Write a function to get the answer to Q2. Do not just echo the answer.
          cat pagecounts-20141101-000000 | wc -l
}

# Before filtering, what was the total number of requests made to all 
# of wikipedia (all subprojects, all elements, all languages) during 
# the hour covered by the file pagecounts-20141101-000000
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_3() {
	# Write a function to get the answer to Q3. Do not just echo the answer.
           awk -F " " '{ sum += $3 } END { print sum }' pagecounts-20141101-000000
}

# How many lines emerged after applying all the filters?
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_4() {
	# Write a function to get the answer to Q4. Do not just echo the answer.
	cat output | wc -l
}

# What was the most popular article in the filtered output?
# Run your commands/code to process the dataset and echo a 
# single word to standard output
answer_5() {
	# Write a function to get the answer to Q5. Do not just echo the answer.
	sort -rnk2,2 output>temp1
        awk -F" " ' NR==1,NR==1{ print $1 } ' temp1
}

# How many views did the most popular article get?
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_6() {
	# Write a function to get the answer to Q6. Do not just echo the answer.
	sort -rnk2,2 output>temp2
        awk -F " " ' NR==1,NR==1{ print $2 } ' temp2
}

# What is the count of the most popular movie in the filtered output? 
# (Hint: Entries for movies have “(film)” in the article name)
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_7() {
	# Write a function to get the answer to Q7. Do not just echo the answer.
	awk -F " " ' { if($1 ~ "(film)") print } ' output>temp3
        sort -rnk2,2 temp3 > temp4
        awk -F " " ' NR==1,NR==1{ print $2 } ' temp4
}

# How many articles have more than 10000 views in the filtered output?
# Run your commands/code to process the dataset and echo a 
# single number to standard output
answer_8() {
	# Write a function to get the answer to Q8. Do not just echo the answer.
	awk -F " " ' { if($2 > 10000) sum += 1 } END { print sum } ' output
}

# DO NOT MODIFY ANYTHING BELOW THIS LINE

answer_1 &> /dev/null
echo "{"

if [ -f 'output' ]
then
	echo -en ' '\"answer1\": \"'output' file created\"
	echo ","
else
	echo -en ' '\"answer1\": \"No 'output' file created\"
	echo ","
fi

echo -en ' '\"answer2\": \"`answer_2`\"
echo ","

echo -en ' '\"answer3\": \"`answer_3`\"
echo ","

echo -en ' '\"answer4\": \"`answer_4`\"
echo ","

echo -en ' '\"answer5\": \"`answer_5`\"
echo ","

echo -en ' '\"answer6\": \"`answer_6`\"
echo ","

echo -en ' '\"answer7\": \"`answer_7`\"
echo ","

echo -en ' '\"answer8\": \"`answer_8`\"
echo
echo  "}"
