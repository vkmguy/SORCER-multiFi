/*
 * Copyright 2019 the original author or authors.
 * Copyright 2019 SorcerSoft.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.service;

import net.jini.core.transaction.Transaction;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ModelStrategy;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.ent.AnalyzerEntry;
import sorcer.core.plexus.FidelityManager;
import sorcer.service.*;
import sorcer.service.Discipline;
import sorcer.service.modeling.SuperviseException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Governance implements Contextion, Transdiscipline, Dependency {

	private static final long serialVersionUID = 1L;

	protected static Logger logger = LoggerFactory.getLogger(Governance.class.getName());

	private static int count = 0;

	protected Uuid id = UuidFactory.generate();

	protected  String name;

    // the input of this governance
    protected Context input;

    // the output of this governance
    protected Context output;

    protected Fi multiFi;

	protected Morpher morpher;

    // active disciplnes
    protected Paths disciplnePaths = new Paths();

	protected ServiceFidelity supervisorFi;

	protected Fidelity<AnalyzerEntry> analyzerFi;

	protected ServiceFidelity contextMultiFi;

	protected Map<String, Discipline> disciplines = new HashMap<>();

	// dependency management for this governance
	protected List<Evaluation> dependers = new ArrayList<Evaluation>();

	private FidelityManager fiManager;

	protected MogramStrategy mogramStrategy;

	// context input connector
	protected Context inConnector;

	// context output connector
	protected Context outConnector;

	protected Context scope;

    public Governance() {
        this(null);
    }

    public Governance(String name) {
        if (name == null) {
            this.name = getClass().getSimpleName() + "-" + count++;
        } else {
            this.name = name;
        }
		mogramStrategy = new ModelStrategy(this);
    }

    public Governance(String name, Discipline[] disciplines) {
        this(name);
        for (Discipline disc : disciplines) {
                this.disciplines.put(disc.getName(), disc);
				disciplnePaths.add(new Path(disc.getName()));
        }
    }

	public Governance(String name, List<Discipline> disciplines) {
		this(name);
		for (Discipline disc : disciplines) {
			this.disciplines.put(disc.getName(), disc);
			disciplnePaths.add(new Path(disc.getName()));
		}
	}

    public Context getOutput() {
        return output;
    }

    public void setOutput(Context output) {
        this.output = output;
    }

    public Paths getDisciplnePaths() {
        return disciplnePaths;
    }

    public void setDisciplnePaths(Paths disciplnePaths) {
        this.disciplnePaths = disciplnePaths;
    }

    public Map<String, Discipline> getDisciplines() {
		return disciplines;
	}

	public Discipline getDiscipline(String name) {
		return disciplines.get(name);
	}

    public Supervisor getSuperviser() {
        return (Supervisor) supervisorFi.getSelect();
    }

    public void setSuperviser(Governor superviser) {
		if (supervisorFi == null) {
			supervisorFi = new ServiceFidelity(new Service[]{superviser});
		} else {
			supervisorFi.addSelect(superviser);
			supervisorFi.setSelect(superviser);
		}
    }

	public void selectSuperviser(String name) throws ConfigurationException {
		supervisorFi.selectSelect(name);
	}
	// default instance new Return(Context.RETURN);
	protected Context.Return contextReturn;

	public ServiceFidelity getContextMultiFi() {
		return contextMultiFi;
	}

	public void setContextMultiFi(ServiceFidelity contextMultiFi) {
		this.contextMultiFi = contextMultiFi;
	}

	public Context getInput() throws ContextException {
		// if no contextMultiFi then return direct input
		if (contextMultiFi == null || contextMultiFi.getSelect() == null) {
			return input;
		}
		input = (Context) contextMultiFi.getSelect();
		return input;
	}

	public Context setInput(Context input) throws ContextException {
		if (contextMultiFi == null) {
			contextMultiFi = new ServiceFidelity();
		}
		contextMultiFi.getSelects().add(input);
		contextMultiFi.setSelect(input);

		this.input = input;
		return input;
	}

	@Override
	public Context getContext() throws ContextException {
		return input;
	}

	@Override
	public void setContext(Context input) throws ContextException {
		setInput(input);
	}

	@Override
	public Context appendContext(Context context) throws ContextException, RemoteException {
		return input.appendContext(context);
	}

	@Override
	public Context getContext(Context contextTemplate) throws RemoteException, ContextException {
		return null;
	}

	@Override
	public Context appendContext(Context context, String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context getContext(String path) throws ContextException, RemoteException {
		return null;
	}

	@Override
	public Context.Return getContextReturn() {
		return contextReturn;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Fi getMultiFi() {
		return multiFi;
	}

	@Override
	public Morpher getMorpher() {
		return morpher;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public Fidelity<AnalyzerEntry> getAnalyzerFi() {
		return analyzerFi;
	}

	public void setAnalyzerFi(Fidelity<AnalyzerEntry> analyzerFi) {
		this.analyzerFi = analyzerFi;
	}

	public List<Discipline> getDisciplineList() {
		List<Discipline> discList = new ArrayList<>();
		for (Discipline disc : disciplines.values()) {
			if (disc instanceof Discipline) {
				discList.add(disc);
			}
		}
		return discList;
	}

	public Fidelity<AnalyzerEntry> setAnalyzerFi(Context context) throws ConfigurationException {
		if(analyzerFi == null) {
			Object mdaComponent = context.get(Context.MDA_PATH);
			if (mdaComponent != null) {
				if (mdaComponent instanceof AnalyzerEntry) {
					analyzerFi = new Fidelity(((AnalyzerEntry) mdaComponent).getName());
					analyzerFi.addSelect((AnalyzerEntry) mdaComponent);
					analyzerFi.setSelect((AnalyzerEntry) mdaComponent);
				} else if (mdaComponent instanceof ServiceFidelity
					&& ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
					analyzerFi = (Fidelity) mdaComponent;
				}
			}
		}
		((ServiceContext)context).getMogramStrategy().setExecState(Exec.State.INITIAL);
		if (output == null) {
			output = new ServiceContext(name);
		}
		return analyzerFi;
	}

	public Context getInConnector() {
		return inConnector;
	}

	public void setInConnector(Context inConnector) {
		this.inConnector = inConnector;
	}

	public Context getOutConnector() {
		return outConnector;
	}

	public void setOutConnector(Context outConnector) {
		this.outConnector = outConnector;
	}

	public FidelityManager getFiManager() {
		return fiManager;
	}

	public void setFiManager(FidelityManager fiManager) {
		this.fiManager = fiManager;
	}

	public MogramStrategy getMogramStrategy() {
		return mogramStrategy;
	}

	public void setModelStrategy(MogramStrategy strategy) {
		mogramStrategy = strategy;
	}

	@Override
	public Object execute(Arg... args) throws ServiceException, RemoteException {
		Context context = Arg.selectContext(args);
		Context out = null;
		out = evaluate(context, args);
		return out;
	}

	@Override
	public Context evaluate(Context context, Arg... args) throws EvaluationException, RemoteException {
		Context out = null;
		Context cxt = context;
			if (cxt == null) {
				try {
					cxt = getInput();
				} catch (ContextException e) {
					throw new EvaluationException(e);
				}
			}

		// set mda if available
		try {
			if (analyzerFi == null) {
				setAnalyzerFi(cxt);
			}
			ModelStrategy strategy = ((ModelStrategy) cxt.getMogramStrategy());
			if (analyzerFi != null) {
				strategy.setExecState(Exec.State.RUNNING);
				// select mda Fi if provided
				List<Fidelity> fis = Arg.selectFidelities(args);
				for (Fi fi : fis) {
					if (analyzerFi.getName().equalsIgnoreCase(fi.getPath())) {
						analyzerFi.selectSelect(fi.getName());
					}
				}
				logger.info("*** analyzerFi: {}", analyzerFi.getSelect().getName());
				out = ((Supervisor)supervisorFi.getSelect()).supervise(cxt, args);
				strategy.setExecState(Exec.State.DONE);
			} else {
				out = ((Supervisor)supervisorFi.getSelect()).supervise(cxt, args);
			}
			((ModelStrategy)mogramStrategy).setOutcome(output);
		} catch (SuperviseException | ConfigurationException e) {
			throw new EvaluationException(e);
		}
		return out;
	}

	@Override
	public Context exert(Transaction txn, Arg... args) throws ContextException, RemoteException {
		return evaluate(input, args);
	}

	public void reportException(String message, Throwable t) {
		mogramStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, ProviderInfo info) {
		// reimplement in sublasses
		mogramStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, Exerter provider) {
		// reimplement in sublasses
		mogramStrategy.addException(t);
	}

	public void reportException(String message, Throwable t, Exerter provider, ProviderInfo info) {
		// reimplement in sublasses
		mogramStrategy.addException(t);
	}

	public Governance addDepender(Evaluation depender) {
		if (this.dependers == null)
			this.dependers = new ArrayList<Evaluation>();
		dependers.add(depender);
		return this;
	}

	@Override
	public void addDependers(Evaluation... dependers) {
		if (this.dependers == null)
			this.dependers = new ArrayList<Evaluation>();
		for (Evaluation depender : dependers)
			this.dependers.add(depender);
	}

	@Override
	public List<Evaluation> getDependers() {
		return dependers;
	}

	@Override
	public Map<String, Discipline> getChildren() {
		return disciplines;
	}

	@Override
	public Discipline getChild(String name) {
		return disciplines.get(name);
	}

	@Override
	public Context getScope() {
		return scope;
	}

	@Override
	public void setScope(Context scope) {
		this.scope = scope;
	}

	@Override
	public List<Contextion> getContextions(List<Contextion> contextionList) {
		for (Contextion e : disciplines.values()) {
			e.getContextions(contextionList);
		}
		contextionList.add(this);
		return contextionList;
	}

	@Override
	public void selectFidelity(Fidelity fi) throws ConfigurationException {

	}
}
