# Look for prose issues
prose.lint_files markdown_files

# Look for spelling issues
prose.ignored_words = ["ev3"]
prose.check_spelling markdown_files

# Ensure a clean commits history
if git.commits.any? { |c| c.message =~ /^Merge branch/ }
  fail('Please rebase to get rid of the merge commits in this PR')
end