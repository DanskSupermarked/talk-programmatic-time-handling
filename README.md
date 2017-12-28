# Markdown to slideshow

This is a reduced distribution of Andrew Goldstone's elsmd, a framework for 
producing PDF slides from Markdown via Pandoc and Beamer. It differs primarily 
by supporting only *notes*-style slides and secondarily by recommending a 
self-contained distribution for reproducibility.

## Usage

1. Fork this repository.
1. Replace `notes/notes-sample.md` with your presentation

For rudimentary automatic recompilation, run `./watch` in a separate terminal. 
For this to be worthwhile your PDF viewer needs to refresh automatically; 
Evince does this, for instance. By default that script compiles *only* the 
presentation slides for faster feedback cycle but it can build everything if 
you pass the `all` argument.

For more effective Vim editing install `vim-pandoc` and `vim-pandoc-syntax`, 
for instance with vim-plug:

```vim
Plug 'vim-pandoc/vim-pandoc'
Plug 'vim-pandoc/vim-pandoc-syntax'
```

### Notes

- The original distribution of elsmd includes a small patch to 
  `vim-pandoc-syntax` related to multiline `\note{}`. I removed this because I 
  don't think it helps the underlying issue, which is that those contexts are 
  LaTeX, enough.
- You can't use Markdown in a LaTeX context. This shouldn't be surprising; what 
  might be surprising is that you easily end up in a LaTeX context.
- Some LaTeX things don't work even in certain LaTeX contexts. For instance, 
  `\only{<p0-p1>}{}` and `\onslide{<p0-p1>}` break so much as to be nearly 
  useless.

## Resources

- For motivation behind and history of elsmd refer to 
  https://andrewgoldstone.com/blog/2014/12/24/slides/
- For general usage instructions and details about the file structure, refer to 
  https://github.com/agoldst/elsmd
- For Pandoc specifics refer to https://pandoc.org/MANUAL.html
- vim-pandoc: https://github.com/vim-pandoc/vim-pandoc
- vim-pandoc-syntax: https://github.com/vim-pandoc/vim-pandoc-syntax
- vim-plug: https://github.com/junegunn/vim-plug
- draw.io: https://www.draw.io/
- PlantUML: http://plantuml.com/
