//package edu.uob;
//
//import edu.uob.nodes.ASTNode;
//import edu.uob.nodes.LeafNode;
//import edu.uob.nodes.BranchNode;
//import edu.uob.parse.Lexer;
//import edu.uob.parse.Parser;
//import edu.uob.parse.TokenStream;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class ParserTests {
//    private Lexer lexer;
//
//    @BeforeEach
//    public void setUp() {
//        lexer = new Lexer();
//    }
//
//    @Test
//    public void testUseCommand() {
//        String cmd = "USE myDB;";
//        TokenStream stream = new TokenStream(lexer.tokenize(cmd));
//        Parser parser = new Parser(stream);
//        Stmt head = parser.parse();
//
//        List<ASTNode> children = List.of(new LeafNode(LeafNode.LeafType.DATABASE_NAME, "myDB"));
//        BranchNode answer = new BranchNode(BranchNode.BranchType.USE, children);
//
//        assertEquals(answer, head);
//    }
//
//    @Test
//    public void testSelectCommand() {
//        String cmd = "SELECT id, mark FROM students WHERE age >= 25 AND name == 'Alex';";
//        TokenStream stream = new TokenStream(lexer.tokenize(cmd));
//        Parser parser = new Parser(stream);
//        BranchNode actual = parser.parse();
//
//        // Create Expected Output
//        ASTNode expected = new BranchNode(
//            BranchNode.BranchType.SELECT,
//            List.of(
//                new BranchNode(BranchNode.BranchType.ATTRIBUTE_LIST, List.of(
//                    new LeafNode(LeafNode.LeafType.ATTRIBUTE_NAME, "id"),
//                    new LeafNode(LeafNode.LeafType.ATTRIBUTE_NAME, "mark")
//                )),
//                new LeafNode(LeafNode.LeafType.TABLE_NAME, "students"),
//                new BranchNode(BranchNode.BranchType.AND, List.of(
//                    new BranchNode(BranchNode.BranchType.GTE, List.of(
//                        new LeafNode(LeafNode.LeafType.ATTRIBUTE_NAME, "age"),
//                        new LeafNode(LeafNode.LeafType.INTEGER_LITERAL, "25")
//                    )),
//                    new BranchNode(BranchNode.BranchType.EQ, List.of(
//                        new LeafNode(LeafNode.LeafType.ATTRIBUTE_NAME, "name"),
//                        new LeafNode(LeafNode.LeafType.STRING_LITERAL, "Alex")
//                    ))
//                ))
//            )
//        );
//
//        assertEquals(actual, expected);
//    }
//}
