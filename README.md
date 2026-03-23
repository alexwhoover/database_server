# Database Server Assignment
- Every command except JOIN has been implemented because I really could not be bothered. 
- There is some ugly casting going on because the tables are stored in text .tab files. In a real database server I'd probably store info about the types of each table attribute in the .tab file.
- I would love some feedback on how I've used the visitor pattern, because I feel I could have used it better.
- I would also love some feedback on the Stmt and Expr structures that I parse the SQL commands into. Could I have used a  better data structure here? I tried to roughly follow what was in Crafting Interpreters.
- I'm aware that I do not have great coverage on my tests but there wasn't enough time.