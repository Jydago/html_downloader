# HTML Downloader
Crawl a website and download all of it's HTML source using Scala.
This is done by starting at the index page and looking for all links in the HTML code that starts with `/` and isn't directing to another www page.
This process will then be repeated for each link in the previous page recursively. 

There is a synchronous version and an asynchronous version.

## Getting started
- Make sure you have Java installed, at least Java 8.
- This project uses [Mill](http://www.lihaoyi.com/mill/index.html) as its build tool.
If you're on OS X or Linux, you can use the bundled mill script, though that means that all commands will have to be run as ./mill.
Otherwise, follow the instructions on the Mill website to install it.

## How to use
- You can run the application using the command `mill app.run sync/async WEBSITE [CRAWL_DEPTH]`.
Choose either sync or async depending on the version you want to use.
To limit the recursive depth when crawling a website, give a max crawl depth through the optional `CRAWL_DEPTH` argument.
- To run tests, use the command `mill app.test`. 

## Project structure
- As mentioned above, this project uses Mill as its build tool.
    - The build definition and project dependencies can be found in `build.sc`.
- [Jsoup](https://jsoup.org/) is used to parse the downloaded HTML code and find links as well as download the HTML code in the synchronous case.
- [Async Http Client](https://github.com/AsyncHttpClient/async-http-client/) is used to download the HTML code in the asynchronous case.
- [OS-Lib](https://github.com/lihaoyi/os-lib) is used to handle filesystem paths.