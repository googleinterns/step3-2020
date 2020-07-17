def process_text(name):
  with open(name + '.txt', 'r') as input:
    lines = input.readlines()
  content = [l.strip() for l in lines]
  comma_removed = [c.replace(',', '') for c in content]
  replace_colon = [s.replace(':', ',') for s in comma_removed]

  with open(name + '.csv', 'w') as output:
    for i in replace_colon:
      output.write('%s\n' % str(i))

process_text('sample_data')
