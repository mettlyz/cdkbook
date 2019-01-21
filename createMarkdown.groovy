// Copyright (c) 2018  Egon Willighagen <egon.willighagen@gmail.com>
//
// GPL v3

input = args[0]

bibliography = new HashMap<String,String>();
def bibLines = new File("references.dat").readLines()
bibLines.each { String line ->
  splitString = '=1. '; fields = []
  fields[0] = line.substring(0,line.indexOf(splitString))
  fields[1] = line.substring(line.indexOf(splitString)+splitString.length())
  bibliography.put(fields[0], fields[1])
}

chapterCounters = new HashMap<String,String>();
chapterCounter = 0
def chapterLines = new File("order.txt").readLines()
chapterLines.each { String line ->
  chapterCounter++
  chapterCounters.put(line, "" + chapterCounter)
}

figureChapters = new HashMap<String,String>();
figureNumbers = new HashMap<String,String>();
def figdataLines = new File("figures.txt").readLines()
figdataLines.each { String line ->
  data = line.split("\t")
  label = data[0]
  chapter = data[1]
  number = data[2]
  figureChapters.put(data[0], data[1])
  figureNumbers.put(data[0], data[2])
}

pkgs = new HashMap<String,String>();
modules = new HashMap<String,String>();
def lines = new File("classinfo.tsv").readLines()
lines.each { String line ->
  data = line.split("\t")
  topic = data[0]
  pkg = data[1]
  module = data[2]
  pkgs.put(topic, pkg)
  modules.put(topic, module)
}

references = new HashMap<String,String>();
bibList = "";
refCounter = 0;
topicCounter = 0;
figureCounter = 0;

context = input.substring(0, input.indexOf("."))
currentChapterCounter = chapterCounters.get(context)

lines = new File(input).readLines()
figureInstructionText = ""
inFigure = false
lines.each { String line ->
  if (inFigure && !line.contains("</figure>")) {
    figureInstructionText += line
    return
  } else if (line.startsWith("<code>")) {
    def instruction = new XmlSlurper().parseText(line)
    def srcLines = new File("code/${instruction.text()}.verbatim.md").readLines()
    srcLines.each { String srcLine -> println srcLine }
  } else if (line.startsWith("<out>")) {
    def instruction = new XmlSlurper().parseText(line)
    println "```plain"
    def srcLines = new File("code/${instruction.text()}.out").readLines()
    srcLines.each { String srcLine -> println srcLine }
    println "```"
  } else if (line.startsWith("<in>")) {
    def instruction = new XmlSlurper().parseText(line)
    def srcLines = new File("${instruction.text()}").readLines()
    srcLines.each { String srcLine -> println srcLine }
  } else if (line.startsWith("<toc>")) {
    def instruction = new XmlSlurper().parseText(line)
    def srcLines = new File("${instruction.text()}").readLines()
    srcLines.each { String srcLine -> println srcLine.replaceAll(".i.md", ".md") }
  } else if (line.contains("<references/>")) {
    println bibList
  } else if (line.startsWith("%%%")) {
    // ignore/remove this line
  } else if (line.contains("</figure>")) {
    figureEnd = line.indexOf("</figure>")
    figureInstructionText += line.substring(0, figureEnd+9)
    // now process the XML
    def figuresInstruction = new XmlSlurper().parseText(figureInstructionText)
    if (figuresInstruction.@label != null && !(figuresInstruction.@label.isEmpty())) {
      println "<a name=\"fig:${figuresInstruction.@label}\"></a>"
    }
    println figuresInstruction.text().trim()
    println "**Figure ${currentChapterCounter}.${figureCounter}**: " + figuresInstruction.@caption
    inFigure = false
  } else if (line.startsWith("<figure")) {
    inFigure = true
    figureCounter++
    figureStart = line.indexOf("<figure")
    figureInstructionText = line.substring(figureStart)
  } else {
    while (line.contains(".i.md")) {
      line = line.replace(".i.md", ".md")
    }
    while (line.contains("<cite>")) {
      citeStart = line.indexOf("<cite>")
      citeEnd = line.indexOf("</cite>")
      cites = line.substring(citeStart+6, citeEnd)
      if (cites.isEmpty()) cites = "?"
      replacement = ""
      if (!references.containsKey(cites)) {
        refCounter++
        references.put(cites, "" + refCounter)
        bibList += "${refCounter}. <a name=\"citeref${refCounter}\"></a>"
        if (bibliography.get(cites) != null) {
          bibList += bibliography.get(cites) + "\n"
        } else {
          bibList += "Missing\n"
        }
        replacement = "<a href=\"#citeref${refCounter}\">${refCounter}</a>"
      } else {
        replacement = Integer.valueOf(references.get(cites))
      }
      line = line.substring(0, citeStart) + replacement + line.substring(citeEnd+7)
    }
    while (line.contains("<topic")) {
      topicCounter++
      topicStart = line.indexOf("<topic")
      topicEnd = line.indexOf("</topic>")
      topicsXML = line.substring(topicStart, topicEnd+8)
      def topicsInstruction = new XmlSlurper().parseText(topicsXML)
      replacement = ""
      if (topicsInstruction.@type == "class" ||
          topicsInstruction.@type == "key") {
        replacement = "`" + topicsInstruction.text() + "`"
      } else {
        replacement = topicsInstruction.text()
      }
      replacement = "<a name=\"tp${topicCounter}\">" + replacement + "</a>"
      line = line.substring(0, topicStart) + replacement + line.substring(topicEnd+8)
    }
    while (line.contains("<class")) {
      classStart = line.indexOf("<class")
      classEnd = line.indexOf("</class>")
      classXML = line.substring(classStart, classEnd+8)
      def classInstruction = new XmlSlurper().parseText(classXML)
      classname = classInstruction.text()
      if (pkgs.containsKey(classname)) {
        replacement = "[`${classname}`](http://cdk.github.io/cdk/latest/docs/api/" +
          pkgs.get(classname).replace(".", "/") + "/${classname}.html)"
      } else {
        replacement = "`" + classname + "`"
      }
      line = line.substring(0, classStart) + replacement + line.substring(classEnd+8)
    }
    while (line.contains("<xref")) {
      xrefStart = line.indexOf("<xref")
      xrefEnd = line.indexOf("</xref>")
      xrefXML = line.substring(xrefStart, xrefEnd+7)
      def xrefInstruction = new XmlSlurper().parseText(xrefXML)
      xrefname = xrefInstruction.text()
      if (figureChapters.containsKey(xrefname)) {
        doc = ""
        if (figureChapters.get(xrefname) != chapter) doc = figureChapters.get(xrefname) + ".md"
        replacement = "[${doc}#fig:${xrefname}](" + figureNumbers.get(xrefname) + ")"
      }
      line = line.substring(0, xrefStart) + replacement + line.substring(xrefEnd+7)
    }
    println line
  }
}
