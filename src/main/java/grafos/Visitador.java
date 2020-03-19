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
import com.github.javaparser.ast.stmt.WhileStmt;
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
	int contadorIf = 1;
	boolean ifActivo = false;
	String nodoActualAux = "";
	
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
		nodoActualAux = nodoActual;
				
		añadirArco(cfg);

		comprobarNodosSalida(cfg);
		
		nodoAnterior = nodoActual;
		
		// Seguimos visitando...
		super.visit(es, cfg);
	}
	
	private void comprobarNodosSalida(CFG cfg) {
		while(!cfg.nodos_control.isEmpty()) {
			if (cfg.tipo_nodos_control.get(cfg.tipo_nodos_control.size()-1)=="salgo-if-sin-else") {
				añadirArco(cfg, cfg.nodos_control.get(cfg.nodos_control.size()-1));
				cfg.nodos_control.remove(cfg.nodos_control.size()-1);
				cfg.tipo_nodos_control.remove(cfg.tipo_nodos_control.size()-1);
			}
			else if (cfg.tipo_nodos_control.get(cfg.tipo_nodos_control.size()-1)=="salgo-if-con-else") {
				añadirArco(cfg, cfg.nodos_control.get(cfg.nodos_control.size()-1));
				cfg.nodos_control.remove(cfg.nodos_control.size()-1);
				cfg.tipo_nodos_control.remove(cfg.tipo_nodos_control.size()-1);
			}	
			else break;
		}
	}
	
	
	// Visitador de whiles
	// Cada while encontrado genera nodos en el CFG	
	@Override
	public void visit(WhileStmt es, CFG cfg)
	{
		// Creamos el nodo actual
		nodoActual = crearNodo("while " + es.getCondition()); 
		final String nodoWhile = nodoActual;
		comprobarNodosSalida(cfg);
		
		añadirArco(cfg);
		nodoAnterior = nodoActual;
		
		es.getBody().accept(this, cfg);
		nodoAnterior = nodoActual;
		nodoActual = nodoWhile;
		añadirArco(cfg);
		nodoAnterior = nodoWhile;

	}
	
	



	// Visitador de ifs
	// Cada if encontrado genera nodos en el CFG	
	@Override
	public void visit(IfStmt es, CFG cfg)
	{
		
		// Creamos el nodo actual
		nodoActual = crearNodo("if " + es.getCondition()); 
		final String nodoIf = nodoActual;
		System.out.println("CONTADOR IFS: " + contadorIf++ + nodoIf);
		
		comprobarNodosSalida(cfg);
		
		añadirArco(cfg);
		
		nodoAnterior = nodoActual;
	
		es.getThenStmt().accept(this, cfg);
		//cfg.nodos_control.add(nodoIf);
		//cfg.tipo_nodos_control.add("salgo-if-sin-else");
		final String ultimoNodoThen = nodoAnterior;
		
		if(es.getElseStmt().isPresent()) {
			nodoAnterior = nodoIf;
			es.getElseStmt().get().accept(this, cfg);
			cfg.tipo_nodos_control.add("salgo-if-con-else");
			cfg.nodos_control.add(ultimoNodoThen);
		}
		else {
			cfg.nodos_control.add(nodoIf);
			cfg.tipo_nodos_control.add("salgo-if-sin-else");
		}

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
	
	private void añadirArcoWhile(CFG cfg, String nodoWhile) {
		// TODO Auto-generated method stub
		
		System.out.println("NODO: " + nodoActual);
		
		String arco = nodoActual + "->" + nodoWhile + ";";
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
