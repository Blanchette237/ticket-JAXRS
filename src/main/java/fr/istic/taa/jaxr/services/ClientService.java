package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.ClientCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Ticket;
import javassist.NotFoundException;

import java.util.List;

public class ClientService {
    private final ClientDao clientDao = new ClientDao();
    private final TicketDao ticketDao = new TicketDao();

    public List<Client> findAll(){
        return clientDao.findAll();
    }

    public Client findOne(Long id) throws NotFoundException {
        Client client = clientDao.findOne(id);
        if(client == null){
            throw new NotFoundException("Client non trouvé");
        }
        return client;
    }

    public long create(ClientCreateDTO dto){
        Client client = new Client();
        client.setName(dto.getName());
        client.setFirstname(dto.getFirstname());
        client.setEmail(dto.getEmail());
        client.setPassword(dto.getPassword());
        clientDao.save(client);
        return client.getUserId();
    }


    public List<Ticket> findTicketsByClient(Long clientId) throws NotFoundException{
        Client client = clientDao.findOne(clientId);
        if(client == null){
            throw new NotFoundException("Client non trouvé");
        }
        return ticketDao.findByClient(client);
    }

    public void delete(Long id) throws NotFoundException {
        Client client = clientDao.findOne(id);
        if(client == null){
            throw new NotFoundException("Client non trouvé");
        }
        clientDao.delete(client);
    }
}
