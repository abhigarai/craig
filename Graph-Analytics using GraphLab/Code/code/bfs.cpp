/**
* This is the skeleton code for bfs.
* Note the save and load functions in each struct and class are used to serialize
* and deserialize the object. If you add members with non-primitive data types in
* a struct or class, you need to implement the save and load functions. For more
* information, please see the serialization section in the API documentation of
* GraphLab.
*/


#include <vector>
#include <string>
#include <fstream>

#include <graphlab.hpp>

/*using namespace graphlab;
*/
const int MAX_LEN = 15;


/**
* The vertex data type and also the message type
*/
struct vertex_data {

	std::vector<int> path;		//for storing the path
	bool visited;				//for keeping record of the node being visited or not



	// add data members here
	// GraphLab will use this function to merge messages 
	vertex_data& operator+=(const vertex_data& other) {
		//decision on path choosing
		// Fill your code here
		

		//code for sorting and selecting the path i.e. merging the messages

		//whichever is shorter and if equal the havinf first smaller node

			if (this->path.size() < other.path.size())			
			{
				this->path = this->path;
			}
			else if (this->path.size() > other.path.size())
			{
				this->path = other.path;
			}
			else if (this->path.size() == other.path.size())
			{
				for (int unsigned i = other.path.size()-1; i >= 0 ; i--)
				{
					if (this->path[i] == other.path[i])
					{
						continue;
					}
					else
					{
						if (this->path[i] > other.path[i])
						{
							this->path = other.path;
							break;
						}
						else
						{
							this->path = this->path;
							break;
						}
					}
				}
			}
		return *this;
	}

	void save(graphlab::oarchive& oarc) const {

		// Fill your code here
		oarc << path << visited;
	}

	void load(graphlab::iarchive& iarc) {
		// Fill your code here

		iarc >> path >> visited;
	}
};

/**
* Definition of graph
*/
typedef graphlab::distributed_graph<vertex_data, graphlab::empty> graph_type;

/* The current id we are processing */
int id;

/**
* The bfs program.
*/
class bfs :
	public graphlab::ivertex_program<graph_type,
	graphlab::empty,
	vertex_data>  {
private:
	vertex_data msg;
public:
	void init(icontext_type& context, const vertex_type& vertex,
		const message_type& msg) {
		// Fill your code here
		this->msg = msg;
		this->msg.path.push_back(vertex.id());			//assign the id of the current vertex to path
	}

	// no gather required
	edge_dir_type gather_edges(icontext_type& context,
		const vertex_type& vertex) const {
		return graphlab::NO_EDGES;			//no gather as verything is within the node
	}


	void apply(icontext_type& context, vertex_type& vertex,
		const gather_type& gather_result) {
		// Fill your code here
		vertex.data() = msg;					//assign msg to vertex data
		vertex.data().visited = true;			// make visited as true
	}

	// do scatter on all the in-edges
	edge_dir_type scatter_edges(icontext_type& context,
		const vertex_type& vertex) const {
		return graphlab::IN_EDGES;			//scatter accross in edges
	}

	void scatter(icontext_type& context, const vertex_type& vertex,
		edge_type& edge) const {
		// Fill your code here
		if (edge.source().data().visited==false)		//scatter only to edges not visited
		{
			context.signal(edge.source(), msg);				// signal only those edges
		}
	}

	void save(graphlab::oarchive& oarc) const {
		// Fill your code here
		oarc << msg;
	}

	void load(graphlab::iarchive& iarc) {
		// Fill your code here
		iarc >> msg;
	}
};

void initialize_vertex(graph_type::vertex_type& vertex) {
	// Fill your code here
	// You should initialize the vertex data here

	vertex.data().visited = false;
}

struct shortest_path_writer {
	std::string save_vertex(const graph_type::vertex_type& vtx) {
		// You should print the shortest path here
		
		std::string res;
		std::stringstream str;
		
		if (vtx.data().path.size()!=0)	//if path is not 0
		{
			int i = vtx.data().path.size() - 1;		//the last index for a particular path
			int src = vtx.data().path[i];		//the source
			int dst = vtx.data().path[0];		//the destination 
										//for a path

			//print src and dst
			str << src << "\t";
			str << dst << "\t";


			for (; i >= 0; i--)
			{
				if (i == 0)
				{
					str << vtx.data().path[i];
				}
				else
				{
					str << vtx.data().path[i] << " ";
				}
			}
			str << "\n";

			res = str.str();
		}
		else
		{
			res = "";
		}
		return res;
	}

	std::string save_edge(graph_type::edge_type e) { return ""; }
};

int main(int argc, char** argv) {
	// Initialize control plain using mpi
	graphlab::mpi_tools::init(argc, argv);
	graphlab::distributed_control dc;
	global_logger().set_log_level(LOG_INFO);

	// Parse command line options -----------------------------------------------
	graphlab::command_line_options
		clopts("bfs algorithm");
	std::string graph_dir;
	std::string format = "snap";
	std::string saveprefix;
	std::string top_ids;

	clopts.attach_option("graph", graph_dir,
		"The graph file.");
	clopts.attach_option("format", format,
		"graph format");
	clopts.attach_option("top", top_ids,
		"The file which contains top 10 ids");
	clopts.attach_option("saveprefix", saveprefix,
		"If set, will save the result to a "
		"sequence of files with prefix saveprefix");

	if (!clopts.parse(argc, argv)) {
		dc.cout() << "Error in parsing command line arguments." << std::endl;
		return EXIT_FAILURE;
	}


	// Build the graph
	graph_type graph(dc, clopts);

	dc.cout() << "Loading graph in format: " << format << std::endl;
	graph.load_format(graph_dir, format);

	// must call finalize before querying the graph
	graph.finalize();
	dc.cout() << "#vertices:  " << graph.num_vertices() << std::endl
		<< "#edges:     " << graph.num_edges() << std::endl;

	graphlab::synchronous_engine<bfs> engine(dc, graph, clopts);
	char id_str[MAX_LEN];
	std::ifstream fin(top_ids.c_str());
	while (fin >> id) {
		graph.transform_vertices(initialize_vertex);

		/*
		* add your implementation here
		*/

		engine.signal(id);
		engine.start();

		std::string tmp = saveprefix;
		tmp += '_';
		sprintf(id_str, "%d", id);
		tmp += id_str;
		graph.save(tmp,
			shortest_path_writer(),
			false,   // do not gzip
			true,    // save vertices
			false,   // do not save edges
			1);      // one output file per machine
	}
	
	fin.close();


	//



	// Tear-down communication layer and quit
	graphlab::mpi_tools::finalize();
	return EXIT_SUCCESS;
} // End of main
