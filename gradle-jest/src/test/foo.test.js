const foo = require("../main/foo/foo")

test('expect foo.main() returning 12', () => {
  expect(foo.main()).toBe(12);
});