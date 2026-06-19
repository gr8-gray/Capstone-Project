# Internal-only MySQL for the Smart Expense Tracker (Fly 6PN).
# bind-address=* makes mysqld listen on IPv4 AND IPv6 so the web app can reach
# us over Fly's private network (dicks-expense-mysql.internal resolves to AAAA).
FROM mysql:8.0
CMD ["mysqld", "--bind-address=*"]
