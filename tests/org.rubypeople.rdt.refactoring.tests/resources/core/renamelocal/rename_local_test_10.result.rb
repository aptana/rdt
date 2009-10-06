def test arg1, a = 5, *arg3, &arg4
  puts arg1 + a - arg3 * arg4
end