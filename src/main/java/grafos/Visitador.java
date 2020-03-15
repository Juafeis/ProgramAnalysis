package grafos;
	
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class Visitador extends VoidVisitorAdapter<CFG>
{	
	/********************************************************/
	/********************** Atributos ***********************/
	/********************************************************/
	
	// Usamos un contador para numerar las instrucciones
	int contador=1;
	String nodoAnterior = "Start";
	String nodoActual = "";
	String nodoIf = "";
	boolean ifActivo = false;
	
	/********************************************************/
	/*********************** Metodos ************************/
	/********************************************************/

		// Visitador de métodos
	// Este visitador añade el nodo final al CFG	
	@Override	
	public void visit(MethodDeclaration methodDeclaration, CFG cfg)
	{
	    // Visitamos el método
		super.visit(methodDeclaration, cfg);
		
		// Añadimos el nodo final al CFG
		cfg.arcos.add(nodoAnterior+"-> Stop;");
	}
	
	// Visitador de expresiones
	// Cada expresión encontrada genera un nodo en el CFG	
	@Override
	public void visit(ExpressionStmt es, CFG cfg)
	{
		// Creamos el nodo actual
		nodoActual = crearNodo(es); 
				
		añadirArco(cfg);
		
		
		
		//System.out.println(cfg.nodos_control);
		
		
		if(cfg.nodos_in_nodos_control.size()>0) {
			//System.out.println(cfg.nodos_in_nodos_control.get(cfg.nodos_in_nodos_control.size()-1).size());
			if(cfg.nodos_in_nodos_control.get(cfg.nodos_in_nodos_control.size()-1).size()>0) {
				cfg.nodos_in_nodos_control.get(cfg.nodos_in_nodos_control.size()-1).remove(0);
			}
			else {
				for(int i = cfg.nodos_in_nodos_control.size()-1; i >= 0 ; i--) {
					System.out.println(cfg.nodos_control);
					System.out.println(cfg.nodos_in_nodos_control);
					if(cfg.nodos_in_nodos_control.get(i).size()==0) {
						System.out.println("nodo actual " + nodoActual);
						añadirArco(cfg, cfg.nodos_control.get(i));
						cfg.nodos_in_nodos_control.get(i).remove(0);
						cfg.nodos_in_nodos_control.remove(i);
						cfg.nodos_control.remove(i);
						
					}
				}

			}
			
		}
		nodoAnterior = nodoActual;
		
		// Seguimos visitando...
		super.visit(es, cfg);
	}
	
	// Visitador de ifs
	// Cada if encontrado genera nodos en el CFG	
	@Override
	public void visit(IfStmt es, CFG cfg)
	{
		// Creamos el nodo actual
		nodoActual = crearNodo("if " + es.getCondition()); 
		
		cfg.nodos_control.add(nodoActual);
		cfg.tipo_nodos_control.add("if");
		
		añadirArco(cfg);
		
		nodoAnterior = nodoActual;
		
		List<String> nodos = new ArrayList<String>();
		for (Node child : es.getThenStmt().getChildNodes()) {
			if(child instanceof IfStmt) {
				System.out.println("NO Añado hijo: " + child.toString());
				}
			else {
				System.out.println("Añado hijo: " + child.toString());
				nodos.add(child.toString());
			}
				
				
		}
		System.out.println(nodos);
		cfg.nodos_in_nodos_control.add(nodos);
		
		es.getThenStmt().accept(this, cfg);
		if(es.getElseStmt().isPresent()) {
			es.getElseStmt().get().accept(this, cfg);
			//nodoElse = crearNodo("else " + es.getElseStmt());
		}
		
		//añadirArco(cfg, nodoAnterior);
		// Seguimos visitando...
		//super.visit((AssertStmt) es.getThenStmt(), cfg);
		

	}
	
	// Crear Arco Secuencial
	private void añadirArco(CFG cfg)
	{
		System.out.println("NODO: " + nodoActual);
		
		String arco = nodoAnterior + "->" + nodoActual + ";";
		cfg.arcos.add(arco);
	}
	
	// Crear Arco IF
	private void añadirArco(CFG cfg, String nodoIf)
	{
		System.out.println("NODO: " + nodoActual);
		
		String arco = nodoIf + "->" + nodoActual + ";";
		cfg.arcos.add(arco);
		
		
		
	}
	


	// Crear nodo
	// Añade un arco desde el nodo actual hasta el último control
	private String crearNodo(Object objeto)
	{
		return "\"("+ contador++ +") "+quitarComillas(objeto.toString())+"\"";
	}
	
	// Sustituye " por \" en un string: Sirve para eliminar comillas.
	private static String quitarComillas(String texto)
	{
	    return texto.replace("\"", "\\\"");
	}
	
	// Dada una sentencia, 
	// Si es una �nica instrucci�n, devuelve un bloque equivalente 
	// Si es un bloque, lo devuelve
	private BlockStmt convertirEnBloque(Statement statement)
	{
		if (statement instanceof BlockStmt)
			return (BlockStmt) statement;

		BlockStmt block = new BlockStmt();
		NodeList<Statement> blockStmts = new NodeList<Statement>();
		blockStmts.add(statement);

		block.setStatements(blockStmts);

		return block;
	}
	
}
