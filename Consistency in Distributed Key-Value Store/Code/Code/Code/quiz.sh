#!/bin/bash

######################################################################
# Quiz script for Project 3 module 5 - Fill in the functions below ###
# for each question.                                               ###
######################################################################

# Qustion 1
# What does strong consistency that you implemented provide?
# A. High throughput and high latency
# B. High throughput and low latency
# C. Low throughput and low latency
# D. Low throughput and high latency
# echo your choice. (ex. echo A)
answer_1() {
    echo
}

# Qustion 2
# What does causal consistency that you implemented provide compared to 
# strong consistency?
# A. High throughput and high latency
# B. High throughput and low latency
# C. Low throughput and low latency
# D. Low throughput and high latency
# echo your choice. (ex. echo A)
answer_2() {
    echo
}

# Qustion 3
# Let's say there's a stock exchange application, where the stock prices are 
# modified. No two transactions should be able to modify the price of a stock 
# simultaneously. Which of the following consistency model will you choose?
# A. Strong Consistency
# B. Causal Consistency
# C. Eventual Consistency
# echo your choice. (ex. echo A)
answer_3() {
    echo
}

# Qustion 4
# The Domain Name System (DNS) guarantees that if a domain name has changed 
# its IP adress, at some point in time all the servers in DNS will point to 
# the new IP address. What consistency model do you think the DNS follows:
# A. Strong Consistency
# B. Eventual Consistency
# echo your choice. (ex. echo A)
answer_4() {
    echo
}


# DO NOT MODIFY ANYTHING BELOW THIS LINE

echo "{"

echo -en ' '\"answer1\": \"`answer_1`\"
echo ","

echo -en ' '\"answer2\": \"`answer_2`\"
echo ","

echo -en ' '\"answer3\": \"`answer_3`\"
echo ","

echo -en ' '\"answer4\": \"`answer_4`\"
echo
echo  "}"


