package io.jpower.sgf.common.biz;

import io.jpower.sgf.collection.IntHashMap;

/**
 * 用来过滤关键词
 *
 * @author zheng.sun
 */
public class WordFilter {

    /**
     * 进行过滤时标记起始位置
     */
    private static final char SIGN_START = 1 << 1;

    /**
     * 进行过滤时标记结束位置
     */
    private static final char SIGN_END = 1 << 2;

    /**
     * 不是过滤词
     */
    private static final int WORD_NONE = 0;

    /**
     * 表示是一个过滤词
     */
    private static final int WORD_FILTER = 1;

    private static final char DEFAULT_REPLACE_CHAR = '*';

    private WordNode root = new WordNode(WORD_NONE);

    /**
     * 初始化关键词
     *
     * @param words
     */
    public void init(String[] words) {
        for (String w : words) {
            int wordLen = w.length();
            if (wordLen < 1) {
                continue;
            }
            addWord(root, w, WORD_FILTER);
        }
    }

    private void addWord(WordNode root, String word, int wordSign) {
        WordNode p = root;
        int wordLen = word.length();
        char c;
        for (int i = 0; i < wordLen; i++) {
            c = word.charAt(i);
            WordNode nextWord = p.getNextWord(c);
            if (nextWord == null) {
                nextWord = new WordNode(WORD_NONE);
                p.addNextWord(c, nextWord);
            }
            p = nextWord;
        }
        p.wordSign = wordSign;
    }

    /**
     * 检查指定字符串是否合法（也就是不包含关键词）
     *
     * @param word
     * @return 如果合法返回true，否则返回false
     */
    public boolean check(String word) {
        int len = word.length();
        int start = -1;
        WordNode p = root;
        char c;
        for (int i = 0; i < len; ) {
            c = word.charAt(i);
            WordNode nextWord = p.getNextWord(c);
            if (nextWord != null) {
                p = nextWord;
                if (start < 0) {
                    start = i;
                }
                switch (nextWord.wordSign) {
                    case WORD_FILTER:
                        return false; // 检查到关键词

                    default:
                        break;
                }
                i++;
            } else if (start != -1) {
                i = start + 1;
                start = -1;
                p = root;
            } else {
                i++;
            }
        }
        return true;
    }

    /**
     * 清理字符串，将关键词屏蔽掉
     *
     * @param word
     * @return
     */
    public String clean(String word) {
        return clean(word, DEFAULT_REPLACE_CHAR);
    }

    /**
     * 清理字符串，用指定的符号将关键词屏蔽掉
     *
     * @param word
     * @param replaceChar
     * @return
     */
    public String clean(String word, char replaceChar) {
        int len = word.length();
        //char[] chars = word.toCharArray();
        int start = -1;
        WordNode p = root;
        char c;
        char[] newChars = null;
        for (int i = 0; i < len; ) {
            c = word.charAt(i);
            WordNode nextWord = p.getNextWord(c);
            if (nextWord != null) {
                p = nextWord;
                if (start < 0) {
                    start = i;
                }
                switch (nextWord.wordSign) {
                    case WORD_FILTER:
                        if (newChars == null) {
                            newChars = new char[len];
                        }
                        newChars[start] |= SIGN_START;
                        newChars[i] |= SIGN_END;
                        start = -1;
                        p = root;
                        break;

                    default:
                        break;
                }
                i++;
            } else if (start != -1) {
                i = start + 1;
                start = -1;
                p = root;
            } else {
                i++;
            }
        }
        if (newChars == null) {
            return word;
        }

        // 关键词替换
        boolean needChange = false;
        for (int i = 0; i < len; i++) {
            char sign = newChars[i];
            if ((sign & SIGN_START) != 0) {
                needChange = true;
            }
            if (needChange) {
                newChars[i] = replaceChar;
            } else {
                newChars[i] = word.charAt(i);
            }
            if ((sign &= SIGN_END) != 0) {
                needChange = false;
            }
        }
        return new String(newChars);
    }

    private static class WordNode {

        private IntHashMap<WordNode> nextWords;

        public int wordSign;

        public WordNode(int wordSign) {
            this.wordSign = wordSign;
        }

        public WordNode getNextWord(char c) {
            return nextWords != null ? nextWords.get(c) : null;
        }

        public void addNextWord(char c, WordNode node) {
            if (nextWords == null) {
                nextWords = new IntHashMap<>();
            }
            nextWords.put(c, node);
        }

    }

}
