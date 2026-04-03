// 测试正则表达式
console.log('Test 1 - httpRequest(url, params):', /[a-zA-Z_]\w*\s*\([^)]*\)/.test('httpRequest(url, params)'));
console.log('Test 2 - get(x):', /[a-zA-Z_]\w*\s*\([^)]*\)/.test('get(x)'));
console.log('Test 3 - def collect():', /def\s+\w+\s*\(/.test('def collect()'));
console.log('Test 4 - def url = "test":', /\bdef\s+\w+\s*=/.test('def url = "test"'));
console.log('Test 5 - test123123:', /^[a-zA-Z0-9_]+$/.test('test123123'));
console.log('Test 6 - def collect() { return "test" }:', /^[a-zA-Z0-9_]+$/.test('def collect() { return "test" }'));
