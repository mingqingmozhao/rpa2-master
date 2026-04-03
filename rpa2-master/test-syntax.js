// 测试语法检查逻辑

function testSyntaxCheck(code) {
  const errors = []
  const warnings = []
  
  // 检查是否包含有效的 Groovy 代码结构
  const hasFunctionDef = /def\s+\w+\s*\(/.test(code)
  const hasVariableDef = /\bdef\s+\w+\s*=/.test(code)
  const hasMethodCall = /\b[a-zA-Z_]\w*\s*\([^)]*\)/.test(code)
  const hasComment = /\/\/|\/\*|\*\//.test(code)
  const hasString = /["'`][^"'`]*["'`]/.test(code)
  const hasKeyword = /\b(def|if|else|for|while|return|try|catch|throw|new|class|import)\b/.test(code)
  
  // 检查代码是否只是随意输入的无效文本
  const codeWithoutComments = code.replace(/\/\/.*$/gm, '').replace(/\/\*[\s\S]*?\*\//g, '').trim()
  
  console.log('测试代码:', code)
  console.log('hasFunctionDef:', hasFunctionDef)
  console.log('hasVariableDef:', hasVariableDef)
  console.log('hasMethodCall:', hasMethodCall)
  console.log('hasComment:', hasComment)
  console.log('hasString:', hasString)
  console.log('hasKeyword:', hasKeyword)
  console.log('codeWithoutComments:', codeWithoutComments)
  
  if (codeWithoutComments) {
    // 检查是否只是一堆字母数字组合（随意输入的文本）
    const linesWithoutComments = codeWithoutComments.split('\n').filter(l => l.trim())
    const allLinesAreGibberish = linesWithoutComments.every(l => {
      const trimmed = l.trim()
      // 如果一行只是字母数字组合，没有空格、运算符等，很可能是乱输入的
      return /^[a-zA-Z0-9_]+$/.test(trimmed) && trimmed.length < 50
    })
    
    console.log('linesWithoutComments:', linesWithoutComments)
    console.log('allLinesAreGibberish:', allLinesAreGibberish)
    
    if (allLinesAreGibberish && linesWithoutComments.length > 0) {
      errors.push('代码看起来像随意输入的文本，不是有效的 Groovy 语法')
      warnings.push('请编写有效的 Groovy 代码，如：def functionName() { ... }')
    }
  }
  
  console.log('错误:', errors)
  console.log('警告:', warnings)
  console.log('---')
}

// 测试用例
testSyntaxCheck('test123123')
testSyntaxCheck('asdfgh')
testSyntaxCheck('def collect() { return "test" }')
testSyntaxCheck('def url = "test"')
testSyntaxCheck('httpRequest(url)')
