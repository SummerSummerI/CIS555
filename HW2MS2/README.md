HW2MS2
======



##Steps
- Using BFS to crawl the links in the sandbox
- Using BerkeleyDB to store the crawled webpages
  - Take a look at the examples coming with the downloaded package.
  - Modify and trim the example to our needs
    - Load ==> Store.put
    - Read ==> Store.get
    - Binding
    - MyDbEnv
  - Design a web page data model to store the content and some other useful information
  - Setup
- Politeness
  - Read the robots.txt file
  - Parse into hashtables
  - Follow specific rules when crawling the web
- User Interface
  - Use servlets to implements
    - User login
    - User register
- XML Display
- RSS Aggregator
  - Use XPaths to match desired items
  - Use XSL to display the matched elements
    - Get the matched XML document
    - Use XSL to find desired elements and display them
