package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport,Airport> visita;
	
	
	public Model() {
		dao= new ExtFlightDelaysDAO();
		idMap= new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap);
	}
	
	//lo metto qui perchè ogni volta che l'utente clicca il bottone
	//creo un grafo da zero
	public void creaGrafo(int x) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//Graphs.addAllVertices(grafo, idMap.values()); //non possiamo prendere tutti gli aereoporti ma dobbiamo filtrarli
		
		//aggiungo vertici filtrati
		Graphs.addAllVertices(grafo,  dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		for(Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())){
				//vedo se c'è già un arco
				DefaultWeightedEdge e= this.grafo.getEdge(r.getA1(), r.getA2());
				
				if(e==null) {// non c'è l'arco tra questi due nodi
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(),r.getN());
					
				} else {
					double pesoVecchio= this.grafo.getEdgeWeight(e);
					double pesoNuovo= pesoVecchio+ r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
				
			}
		}
		System.out.println("Grafo creato");
		System.out.println("#Vertici: "+ grafo.vertexSet().size());
		System.out.println("#Archi: "+ grafo.edgeSet().size());
	}

	public Set<Airport> getVertici() {
		// TODO Auto-generated method stub
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso= new LinkedList<>();
		
		BreadthFirstIterator <Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<>(grafo, a1);
		
		 visita= new HashMap<>();
		 visita.put(a1, a2);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport a1= grafo.getEdgeSource(e.getEdge());
				Airport a2= grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(a1) && !visita.containsKey(a2)) {
					visita.put(a2, a1);
					
				}else if(visita.containsKey(a2) && !visita.containsKey(a1)){
				
					visita.put(a1, a2);
				}
				
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		//VISITA IL GRAFO
		while( it.hasNext()) {
			it.next(); //visito il prossimo iteratore, ma non ci da il percorso
		}
		
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			return null;
		}
		percorso.add(a2);
		Airport step= a2;
		while(visita.get(step)!=null) {
			step= visita.get(step);
		}
		return percorso;
	}
}
